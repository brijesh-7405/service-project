/**
 *
 */
package com.workruit.us.application.models;

import lombok.Data;

import javax.persistence.*;

/**
 * @author Santosh Bhima
 */
@Data
@Entity
@Table(name = "user_import_status")
public class UserImportAsyncStatus extends BaseModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "user_import_status_id")
    private Long userImportAsyncStatusId;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private UserImportJobStatus status;

    @Column(name = "description")
    private String description;

    @Column(name = "csv_key")
    private String csvkey;


    @Column(name = "user_id")
    private Long userId;

    @Column(name = "version")
    private Long version;

    @Column(name = "total_count")
    private Long totalCount;

    @Column(name = "mobile_duplicates")
    private Long mobileDuplicates;

    @Column(name = "email_duplicates")
    private Long emailDuplicates;

    @Column(name = "processed_count")
    private Long processedCount;

    @Column(name = "saved_count")
    private Long savedCount;

    public enum UserImportJobStatus {
        CREATED, INPROGRESS, FAILED, SUCCESS
    }
}
