package com.introproventures.graphql.jpa.query.mutations.fetcher.impl;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.persistence.*;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.PluralAttribute;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.introproventures.graphql.jpa.query.mutations.annotation.GraphQLWriteEntityForRole;
import com.introproventures.graphql.jpa.query.mutations.annotation.GraphQLWriteEntityList;
import com.introproventures.graphql.jpa.query.mutations.annotation.GraphQLWriteType;
import com.introproventures.graphql.jpa.query.mutations.fetcher.IGraphQLJpaEntityInputFetcher;
import com.introproventures.graphql.jpa.query.mutations.fetcher.MutationContext;
import com.introproventures.graphql.jpa.query.schema.ExceptionGraphQLRuntime;
import com.introproventures.graphql.jpa.query.schema.impl.FetcherParams;
import com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaBaseFetcher;
import graphql.language.EnumValue;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.BeanDeserializer;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.module.SimpleModule;

import graphql.schema.DataFetchingEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class GraphQLJpaEntityInputFetcher extends GraphQLJpaBaseFetcher implements IGraphQLJpaEntityInputFetcher {

	protected EntityType<?> entityType;

	Logger logger = LoggerFactory.getLogger(GraphQLJpaEntityInputFetcher.class);

	public static class DeserializeResult {
	    private Object entity;
	    private MutationContext mutationContext = new MutationContext();

        public Object getEntity() {
            return entity;
        }

        public void setEntity(Object entity) {
            this.entity = entity;
        }

        public MutationContext getMutationContext() {
            return mutationContext;
        }
    }
	
	public GraphQLJpaEntityInputFetcher(EntityManager entityManager, FetcherParams fetcherParams, EntityType<?> entityType) {
        super(entityManager, fetcherParams);

        this.entityType = entityType;
    }

	protected Logger getLogger() {
		return logger;
	}

	protected void convertEnumValue(List<Object> lst) {
		for (int i = 0; i < lst.size(); i++) {
			Object ob = lst.get(i);
			if (ob instanceof EnumValue) {
				lst.add(i, ((EnumValue) ob).getName());
			}
			if (ob instanceof Map) {
				convertEnumValue((Map)ob);
			}
			if (ob instanceof List) {
				convertEnumValue((List)ob);
			}
		}
	}

	protected void convertEnumValue(Map<String, Object> mp) {
		for (String key : mp.keySet()) {
			Object ob = mp.get(key);
			if (ob instanceof EnumValue) {
				mp.put(key, ((EnumValue) ob).getName());
			}
			if (ob instanceof Map) {
				convertEnumValue((Map)ob);
			}
			if (ob instanceof List) {
				convertEnumValue((List)ob);
			}
		}
	}


	public DeserializeResult deserialize(DataFetchingEnvironment environment, String argument) {
		try {
		    DeserializeResult res = new DeserializeResult();

			res.getMutationContext().setOperationName(environment.getFieldDefinition().getName());
			Map<String, Object> mp = environment.getArgument(argument);
			convertEnumValue(mp);

			JSONObject json = new JSONObject(mp);

			ObjectMapper oMapper = new ObjectMapper();
			oMapper.registerModule(new JavaTimeModule());
			SimpleModule module = new SimpleModule();
		      
		    module.setDeserializerModifier(new BeanDeserializerModifier() 
		    {
		      @Override public JsonDeserializer<?> modifyDeserializer(DeserializationConfig config, BeanDescription beanDesc, JsonDeserializer<?> deserializer)
		      {
		    	//custom deserialize
		    	if (deserializer instanceof BeanDeserializer)
		    		return new GraphQLStdDeserializer((BeanDeserializer)deserializer, res.getMutationContext());
		    	
		    	return deserializer;
		      }
		    });
		   
		    oMapper.registerModule(module);
		    
		    Object entity = oMapper.readValue(json.toString(), entityType.getJavaType());
		    res.setEntity(entity);
			return res;
		} catch (Exception e) {
			throw new ExceptionGraphQLRuntime(e);
		}
	}

	
	@Override
    public Object get(DataFetchingEnvironment environment) {
		DeserializeResult res = deserialize(environment, "entity");
		return executeMutation(res.getEntity(), res.getMutationContext());
    }
	
	public Object reloadEntity(Object entity) {
		if (entity == null) {
			return null;
		}
		Object id = entityManager.getEntityManagerFactory().getPersistenceUnitUtil().getIdentifier(entity);
		if (id == null) {
			return null;
		}
		return entityManager.find(entity.getClass(), id);
	}

	public Object reloadEntityNotNull(Object entity) {
		if (entity == null) {
			return null;
		}

		Object id = entityManager.getEntityManagerFactory().getPersistenceUnitUtil().getIdentifier(entity);
		if (id == null) {
			throw new ExceptionGraphQLRuntime("Cann`t reload object "+entity.getClass().getSimpleName()+" - id not set");
		}

		Object reloadEntity = entityManager.find(entity.getClass(), id);

		if (reloadEntity == null) {
			throw new  ExceptionGraphQLRuntime("Not found object "+entity.getClass().getSimpleName() + " by id = "+id.toString());
		}

		return reloadEntity;
	}

	public EntityType<?> getEntityType() {
		return entityType;
	}

	public EntityManager getEntityManager() {
		return entityManager;
	}

	public void updateValueAttribute(Attribute attr, Object entity, Object newVal) {
		updateValueAttribute(attr.getName(), entity, newVal, attr.getJavaType(), true);
	}

    public void updateValueAttribute(String attr, Object entity, Object newVal, Class<?> typeValue) {
        updateValueAttribute(attr, entity, newVal, typeValue, true);
    }

	public void updateValueAttribute(String attr, Object entity, Object newVal, Class<?> typeValue, boolean useSetter) {
		try {
			String setter = "set"+StringUtils.capitalize(attr);
			Method method = null;

			try {
			    if (!useSetter) {
			        throw new NoSuchMethodException();
                }
				method = entity.getClass().getMethod(setter, typeValue);
				method.invoke(entity, newVal);
			} catch (NoSuchMethodException e) {
				getLogger().warn("Access to field "+attr+" in class "+entity.getClass().getName()+". check getter");
				Field field = entity.getClass().getDeclaredField(attr);
				field.setAccessible(true);

				field.set(entity, newVal);
			}
		} catch (Exception e) {
			throw new ExceptionGraphQLRuntime(e);
		}
	}

	public Object getValueAttribute(Attribute attr, Object entity) {
		return getValueAttribute(attr.getName(), entity);
	}

	public Object getValueAttribute(String attr, Object entity) {
		try {
			String getter = "get"+StringUtils.capitalize(attr);
			try {
				Method method = entity.getClass().getMethod(getter);
				return method.invoke(entity);
			} catch (NoSuchMethodException e) {
				getLogger().warn("Access to field "+attr+" in class "+entity.getClass().getName()+" check getter");

				Field field = entity.getClass().getDeclaredField(attr);
				field.setAccessible(true);
				return field.get(entity);
			}
		} catch (Exception e) {
			throw new ExceptionGraphQLRuntime(e);
		}
	}

	public void copyEntityFields(Object source, Object dist, List<String> fields) {
		try {
			for (String fieldName : fields) {
				Field field = source.getClass().getDeclaredField(fieldName);
				field.setAccessible(true);
				Object distField = field.get(dist);

                if (distField instanceof Map) {

                    ((Map) distField).clear();
                    ((Map) distField).putAll((Map)field.get(source));
                    continue;
                }

                if (distField instanceof Collection) {
                    ((Collection) distField).clear();
                    ((Collection) distField).addAll((Collection)field.get(source));
                    continue ;
                }

				field.set(dist, field.get(source));
			}
		} catch (Exception e) {
			throw new ExceptionGraphQLRuntime(e);
		}
	}

	public void copyEntityFields(Object source, Object dist, String field) {
		copyEntityFields(source, dist, Arrays.asList(field));
	}



	public void reloadChildEntities(EntityType<?> et, Object entity, MutationContext mutationContext) throws Exception {
		if (entity == null)	return ;

		for (Attribute attr : et.getAttributes()) {
			//getLogger().info("Attr = "+attr.getName());
			Attribute.PersistentAttributeType persistType = attr.getPersistentAttributeType();
			//OneToMany an = attr.getJavaType().getAnnotation(OneToMany.class);


			if (persistType == Attribute.PersistentAttributeType.ONE_TO_ONE ||
					persistType == Attribute.PersistentAttributeType.MANY_TO_ONE ) {
				Object attrVal = getValueAttribute(attr, entity);
				updateValueAttribute(attr, entity, reloadEntityNotNull(attrVal));
				continue;
			}

			if (persistType == Attribute.PersistentAttributeType.ONE_TO_MANY ) {
				Object attrVal = getValueAttribute(attr, entity);
				if (attrVal == null) continue;

				int size = 0;

				if (attrVal instanceof Map) {
					size = ((Map) attrVal).size();
				}

				if (attrVal instanceof Collection) {
					size = ((Collection) attrVal).size();
				}

				if (size > 0) {
					throw  new ExceptionGraphQLRuntime("For field " + attr.getName() + " cannot set objects for insert. (ONE_TO_MANY). Use operation merge or update child entity");
				}

				continue;
			}

			if (persistType == Attribute.PersistentAttributeType.MANY_TO_MANY) {
				EntityType attrType = (EntityType) ((PluralAttribute) attr).getElementType();

				String mappedBychild = null;
				ManyToMany mtm = ((AnnotatedElement) attr.getJavaMember()).getAnnotation(ManyToMany.class);
				if (mtm != null) {
					mappedBychild = mtm.mappedBy();
				}

				Object attrVal = getValueAttribute(attr, entity);

				Function<Object, Object> fun = new Function() {
					private  EntityType attrType;
					private Object parent;
					private String mappedByChildFinal;

					private Function init(EntityType attrType, Object parent, String mappedByChildFinal) {
						this.attrType = attrType;
						this.parent = parent;
						this.mappedByChildFinal = mappedByChildFinal;
						return this;
					}

					@Override
					public Object apply(Object o) {
						return reloadEntityMTMAndSetMappedParent(attrType, o, entity, mappedByChildFinal);
					}
				}.init(attrType, entity, mappedBychild);

				reloadCollection(attrVal, fun);

				//????
			}
		}

	}

	public Object reloadEntityMTMAndSetMappedParent(EntityType<?> et, Object entity, Object parent, String mappedBy) {
		Object reloadEntity = reloadEntityNotNull(entity);

		if (mappedBy != null && mappedBy.length() > 0) {
			Object col = getValueAttribute(mappedBy, reloadEntity);
			Attribute attr = et.getAttribute(mappedBy);
			MapKey mk = ((AnnotatedElement) attr.getJavaMember()).getAnnotation(MapKey.class);
			Object key = null;
			if (mk != null) {
				key = getValueAttribute(mk.name(), parent);
			}

			addManyToManyParent(col, parent, key);
		}

		return reloadEntity;
	}

	public void addManyToManyParent(Object col, Object parent, Object key) {
		if (col instanceof Map) {
			((Map) col).put(key, parent);
			return;
		}

		if (col instanceof Set) {
			((Set) col).add(parent);
			return ;
		}

		if (col instanceof List) {
			((List) col).add(parent);
			return;
		}

		if (col instanceof Collection) {
			((Collection) col).add(parent);
			return;
		}
	}

	public void reloadCollection(Object collection, Function<Object, Object> function) {
		if (collection instanceof Map) {
			((Map) collection).replaceAll((k, v) -> function.apply(v));
			return ;
		}

		if (collection instanceof List) {
			((List) collection).replaceAll(v -> function.apply(v));
			return;
		}

		if (collection instanceof Collection) {
			ArrayList<Object> array = ((Collection<Object>) collection).stream()
					.map(v -> function.apply(v))
					.collect(Collectors.toCollection(ArrayList::new));

			((Collection<Object>) collection).clear();
			((Collection<Object>) collection).addAll(array);
		}
	}

	public static String getAttrMappedBy(Attribute attr) {
        String mappedBy = null;
        OneToMany otm = ((AnnotatedElement) attr.getJavaMember()).getAnnotation(OneToMany.class);
        if (otm != null) {
            mappedBy = otm.mappedBy();
        }

        ManyToMany mtm = ((AnnotatedElement) attr.getJavaMember()).getAnnotation(ManyToMany.class);
        if (mtm != null) {
            mappedBy = mtm.mappedBy();
        }

        OneToOne oto = ((AnnotatedElement) attr.getJavaMember()).getAnnotation(OneToOne.class);
        if (oto != null) {
            mappedBy = oto.mappedBy();
        }

        return mappedBy;
    }

    public static int compare(Attribute a1, Attribute a2) {
	    String m1 = getAttrMappedBy(a1);
        String m2 = getAttrMappedBy(a2);

        if (m1 == null && m2 != null) {
            return -1;
        }

        if (m1 != null && m2 == null) {
            return 1;
        }

        return 0;
    }


    protected boolean checkOperation(GraphQLWriteType operation, GraphQLWriteType[] operList) {
		return Arrays.stream(operList).anyMatch(v -> v.equals(operation) || v.equals(GraphQLWriteType.ALL));
	}

	public void checkAccessWriteOperation(Class cls, GraphQLWriteType operation) {
		if (fetcherParams.getPredicateRole() == null) {
			return ;
		}

		AnnotatedElement annotatedElement = (AnnotatedElement)cls;
		GraphQLWriteEntityForRole writeRoles =  annotatedElement.getAnnotation(GraphQLWriteEntityForRole.class);
		if (writeRoles != null) {
			if (checkOperation(operation, writeRoles.operations())) {
				if (fetcherParams.getPredicateRole().test(writeRoles.value()))
					return;
			}
		}

		GraphQLWriteEntityList writeList =  annotatedElement.getAnnotation(GraphQLWriteEntityList.class);
		if (writeList != null) {
			for (GraphQLWriteEntityForRole wr : writeList.value()) {
				if (checkOperation(operation, wr.operations())) {
					if (fetcherParams.getPredicateRole().test(wr.value()))
						return;
				}
			}
		}

		throw new ExceptionGraphQLRuntime("For entity "+cls.getSimpleName() + " cannot "+operation.name());
	}
}
