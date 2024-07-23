/**
 *
 */
package com.workruit.us.application.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

/**
 * @author Mahesh
 */
@Data
@Entity
@Table(name = "job_question")
public class JobQuestion implements Serializable {
    private static final long serialVersionUID = -6985432295271130270L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @JsonProperty(access = Access.READ_ONLY)
    @Column(name = "question_id")
    private Long questionId;

    @Column(name = "question_type")
    private String questionType;

    @Column(name = "question_title")
    private String questionTitle;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "question_id", nullable = false)
    private Set<JobQuestionValues> questionValues;

    @Column(name = "mandatory")
    private boolean mandatory;

    @Transient
    private Long sortId;

}

