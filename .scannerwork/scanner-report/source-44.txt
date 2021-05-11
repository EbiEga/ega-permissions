/*
 * Copyright 2021-2021 EMBL - European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.ega.permissions.helpers;

import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Component
public class DatasetHelper {

    private EntityManager entityManager;

    //We don't have repositories for Dataset (view) so we use the entity manager for our tests
    public DatasetHelper(EntityManagerFactory entityManagerFactory) {
        this.entityManager = entityManagerFactory.createEntityManager();
    }

    @Transactional
    public void insert(String datasetId, String description, String dacStableId) {
        String sql = "insert into pea.dataset(dataset_id, description, dac_stable_id, double_signature) " +
                "values(?,?,?,'123');";
        this.entityManager.getTransaction().begin();

        Query query = this.entityManager.createNativeQuery(sql)
                .setParameter(1, datasetId)
                .setParameter(2, description)
                .setParameter(3, dacStableId);

        query.executeUpdate();

        this.entityManager.getTransaction().commit();
    }

    @Transactional
    public void removeAll(){
        this.entityManager.getTransaction().begin();
        Query query = this.entityManager.createNativeQuery("truncate table pea.dataset");
        query.executeUpdate();
        this.entityManager.getTransaction().commit();
    }

    public List<String> generateDatasets(String dacStableId, int cant) {
        List<String> datasets = new ArrayList<>(cant);
        for (int i = 1; i <= cant; i++) {
            String datasetId = "EGAD0000" + 1;
            insert(datasetId, "Test Dataset " + datasetId, dacStableId);
            datasets.add(datasetId);
        }
        return datasets;
    }

}
