package com.introproventures.graphql.jpa.query.mutations.fetcher.impl;

import com.introproventures.graphql.jpa.query.schema.ExceptionGraphQLRuntime;
import com.introproventures.graphql.jpa.query.mutations.annotation.GraphQLWriteType;
import com.introproventures.graphql.jpa.query.mutations.fetcher.MutationContext;
import com.introproventures.graphql.jpa.query.schema.impl.FetcherParams;

import javax.persistence.EntityManager;
import javax.persistence.MapKey;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;
import java.lang.reflect.AnnotatedElement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MergeFetcher extends GraphQLJpaEntityInputFetcher {
	public MergeFetcher(EntityManager entityManager, FetcherParams fetcherParams, EntityType<?> entityType) {
        super(entityManager, fetcherParams, entityType);
    }

    @Override
	public Object executeMutation(Object entity, MutationContext mutationContext) {

		try {
			Object resEntity = mergeChildEntities(entityType, entity, null, mutationContext);

			//Object resEntity = entityManager.merge(entity);


			entityManager.flush();

			entityManager.refresh(resEntity);

			return entity;
		} catch (ExceptionGraphQLRuntime e) {
			throw e;
		} catch (Exception e) {
			throw new ExceptionGraphQLRuntime(e);
		}
	}

	public Object mergeChildEntities(EntityType<?> et, Object entity, Object parent, MutationContext mutationContext) throws Exception {
		return mergeChildEntities(et, entity, parent, mutationContext,null);
	}


	public Object mergeChildEntities(EntityType<?> et, Object entity, Object parent, MutationContext mutationContext, String mappedBy) throws Exception {
		if (entity == null)	return null;

		List<String> editFields = mutationContext.getObjectFields(entity);
		if (editFields == null) return entity;

		for (Attribute attr : et.getAttributes()) {
			if (attr.getName().equals(mappedBy)) {
				Attribute.PersistentAttributeType persistType = attr.getPersistentAttributeType();

				if (persistType == Attribute.PersistentAttributeType.ONE_TO_ONE ||
						persistType == Attribute.PersistentAttributeType.MANY_TO_ONE) {
					updateValueAttribute(attr, entity, parent);
					continue;
				}
			}
		}

		Object currentEntity = reloadEntity(entity);

		Boolean isPersist = false;
		if (currentEntity != null) {
			copyEntityFields(entity, currentEntity, editFields);
			entity = currentEntity;
			checkAccessWriteOperation(entity.getClass(), GraphQLWriteType.UPDATE);
		} else {
			isPersist = true;
			checkAccessWriteOperation(entity.getClass(), GraphQLWriteType.INSERT);
		}


        Set<Attribute> sortAttr = et.getAttributes().stream()
                .sorted((a1, a2) -> GraphQLJpaEntityInputFetcher.compare(a1, a2) )
                .collect(Collectors.toSet());

		Map<String, Object> mappedObjects = new HashMap<>();

        for (Attribute attr : sortAttr) {
            if (editFields.indexOf(attr.getName()) >= 0 ) {
                String mappedBychild = getAttrMappedBy(attr);
                if (mappedBychild != null) {
                    mappedObjects.put(attr.getName(), getValueAttribute(attr.getName(), entity));
                    updateValueAttribute(attr.getName(), entity, null, attr.getJavaType(), false);
                }
            }
        }

		for (Attribute attr : et.getAttributes()) {

            //getLogger().info("Attr= " + attr.getName());
			if (editFields.indexOf(attr.getName()) >= 0 || attr.getName().equals(mappedBy)) {

                String mappedBychild = getAttrMappedBy(attr);

                if (isPersist && mappedBychild!=null) {
                    entityManager.persist(entity);
                    isPersist = false;
                }

				Attribute.PersistentAttributeType persistType = attr.getPersistentAttributeType();

				//OneToMany an = attr.getJavaType().getAnnotation(OneToMany.class);

				if (persistType == Attribute.PersistentAttributeType.ONE_TO_ONE ||
					persistType == Attribute.PersistentAttributeType.MANY_TO_ONE) {
					EntityType attrType = (EntityType) ((SingularAttribute) attr).getType();


					if (mappedBy != null && attr.getName().equals(mappedBy)) {
						updateValueAttribute(attr, entity, parent);
						continue;
					}

					Object childEntity = mergeChildEntities(attrType, getValueAttribute(attr, entity), entity, mutationContext, null);
					updateValueAttribute(attr, entity, childEntity);

					continue;
				}

				if (persistType == Attribute.PersistentAttributeType.ONE_TO_MANY ||
					persistType == Attribute.PersistentAttributeType.MANY_TO_MANY) {

					EntityType attrType = (EntityType) ((PluralAttribute) attr).getElementType();

					Object attrVal = null;
					if (mappedObjects.containsKey(attr.getName())) {
                        attrVal = mappedObjects.get(attr.getName());
                    } else {
                        attrVal = getValueAttribute(attr, entity);
                    }

					if (mappedBy != null && attr.getName().equals(mappedBy)) {
						Object key = null;
						MapKey mk = ((AnnotatedElement) attr.getJavaMember()).getAnnotation(MapKey.class);
						if (mk != null) {
							key = getValueAttribute(mk.name(), parent);
						}
						addManyToManyParent(attrVal, parent, key);
						continue;
					}
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
						public Object apply(Object obj) {
							try {
								return mergeChildEntities(attrType, obj, parent, mutationContext, mappedByChildFinal);
							} catch (Exception e) {
								throw new ExceptionGraphQLRuntime(e);
							}
						}
					}.init(attrType, entity, mappedBychild);

					reloadCollection(attrVal, fun);
				}
			}
		}

        if (isPersist) {
            entityManager.persist(entity);
        }

        for (Attribute attr : et.getAttributes()) {
            if (mappedObjects.containsKey(attr.getName())) {
                updateValueAttribute(attr.getName(), entity, mappedObjects.get(attr.getName()), attr.getJavaType(), false);
            }
        }

		//Object res =  entityManager.merge(entity);
		//entityManager.flush();
		return entity;
	}
}
