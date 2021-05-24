/*
 *
 * Copyright 2019 EMBL - European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package uk.ac.ebi.ega.permissions.cache.dto;

import java.util.Objects;

public class DatasetDTO {
    private String datasetId;
    private String dacId;
    private long asserted;

    private DatasetDTO() {
    }

    public DatasetDTO(final String datasetId,
                      final String dacId,
                      final long asserted) {
        this.datasetId = datasetId;
        this.dacId = dacId;
        this.asserted = asserted;
    }

    public String getDatasetId() {
        return datasetId;
    }

    public String getDacId() {
        return dacId;
    }

    public long getAsserted() {
        return asserted;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DatasetDTO that = (DatasetDTO) o;
        return datasetId.equals(that.datasetId) && dacId.equals(that.dacId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(datasetId, dacId);
    }
}
