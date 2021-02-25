package uk.ac.ebi.ega.permissions.helpers;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

public class DatasetHelper {

    private EntityManager entityManager;

    //We don't have repositories for Dataset (view) so we use the entity manager for our tests
    public DatasetHelper(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Transactional
    public void insertDataset(String datasetId, String description, String dacStableId) {
        String sql = "insert into pea.dataset(dataset_id, description, dac_stable_id, double_signature) " +
                "values(?,?,?,'123');";
        this.entityManager.getTransaction().begin();

        Query query  = this.entityManager.createNativeQuery(sql)
                .setParameter(1, datasetId)
                .setParameter(2, description)
                .setParameter(3, dacStableId);

        query.executeUpdate();

        this.entityManager.getTransaction().commit();
    }

    public List<String> generateDatasets(String dacStableId, int cant) {
        List<String> datasets = new ArrayList<>(cant);
        for (int i = 1; i <= cant; i++) {
            String datasetId = "EGAD0000" + 1;
            insertDataset(datasetId, "Test Dataset " + datasetId, dacStableId);
            datasets.add(datasetId);
        }
        return datasets;
    }

}
