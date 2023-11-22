package com.introproventures.graphql.jpa.query.example;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;
import java.util.List;
import org.activiti.cloud.services.query.model.ProcessInstanceEntity;
import org.activiti.cloud.services.query.model.ProcessVariableEntity;
import org.hibernate.jpa.SpecHints;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

@Disabled
@Testcontainers
@SpringBootTest
@Import(TestApplication.class)
class ApplicationTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void contextLoads() {}

    @Test
    @Transactional
    void criteriaQueryVariableValueLoad() {
        EntityType<ProcessInstanceEntity> entityType = entityManager.getMetamodel().entity(ProcessInstanceEntity.class);
        SingularAttribute<?, ?> parentIdAttribute = entityType.getId(Object.class);

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Object[]> query = cb.createQuery(Object[].class);
        Root<?> from = query.from(entityType);

        from.alias("owner");

        Join<?, ?> join = from.join("variables").on(from.get(parentIdAttribute.getName()).in("0"));

        query.multiselect(from.get(parentIdAttribute.getName()), join.alias("variables"));

        EntityGraph<ProcessVariableEntity> entityGraph =
            this.entityManager.createEntityGraph(ProcessVariableEntity.class);
        entityGraph.addAttributeNodes("name", "value");

        TypedQuery<Object[]> typedQuery = entityManager
            .createQuery(query.distinct(true))
            .setHint(SpecHints.HINT_SPEC_LOAD_GRAPH, entityGraph);

        List<Object[]> resultList = typedQuery.getResultList();

        assertThat(resultList)
            .isNotEmpty()
            .extracting(record -> (ProcessVariableEntity) record[1])
            .extracting(ProcessVariableEntity::getName, ProcessVariableEntity::getValue)
            .containsOnly(
                tuple("approverlist", List.of("andrelaksmana")),
                tuple("moduleid", "LBU"),
                tuple("nullable", null),
                tuple("applicationDate", "2023-10-22T00:00:00.000+0000"),
                tuple("applicationId", "232951752337576"),
                tuple("isApproved", true)
            );
    }
}
