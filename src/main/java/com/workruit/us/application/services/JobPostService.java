package com.workruit.us.application.services;

import com.amazonaws.util.StringUtils;
import com.workruit.us.application.configuration.WorkruitException;
import com.workruit.us.application.dto.*;
import com.workruit.us.application.enums.*;
import com.workruit.us.application.exception.CreateJobFailedException;
import com.workruit.us.application.exception.JobNotFoundException;
import com.workruit.us.application.models.*;
import com.workruit.us.application.repositories.*;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class JobPostService {
    private @Autowired JobPostRepository jobPostRepository;
    private @Autowired JobFunctionRepository jobFunctionRepository;
    private @Autowired JobMatchingRepository jobMatchingRepository;
    private @Autowired JobMatchingRepositoryImpl jobMatchingRepositoryImpl;
    private @Autowired JobMatchConsultancyRepository jobMatchConsultancyRepository;
    private @Autowired ConsultancyRepository consultancyRepository;
    private @Autowired ApplicantRepository applicantRepository;
    private @Autowired ApplicantDetailsRepository applicantDetailsRepository;
    private @Autowired UserRepository userRepository;

    private @Autowired ApplicantJobFunctionRepository applicantJobFunctionRepository;
    private @Autowired ApplicantJobSkillRepository applicantJobSkillRepository;
    private @Autowired JobQuestionRepository jobQuestionRepository;

    private @Autowired BlockingQueue<JobForQueue> jobQueue;
    private @Autowired ModelMapper modelMapper;
    private @Autowired JobQuestionValuesRepository jobQuestionValuesRepository;
    private @Autowired AlertService alertService;
    private @Autowired ActivityService activityService;
    private @Autowired WorkExperienceRepository workExperienceRepository;
    private @Autowired EducationHistoryRepository educationHistoryRepository;
    private @Autowired DegreesRepository degreesRepository;
    private @Autowired DepartmentRepository departmentRepository;


    private @Autowired ApplicantSecondaryJobFunctionRepository applicantSecondaryJobFunctionRepository;

    private boolean runJobMatcher = false;

    @Autowired
    private ConsultancyJobStatusRepository consultancyJobStatusRepository;

    public static boolean equalLists(Set<Integer> setOne, Set<Integer> setTwo) {
        if (setOne == null || setTwo == null) {
            return setOne == null && setTwo == null;
        }
        return setOne.containsAll(setTwo);
    }

    public static boolean anyCommonElements(Set<Integer> setOne, Set<Integer> setTwo) {
        if (setOne == null || setTwo == null) {
            return setOne == null && setTwo == null;
        } else {
            for (Integer element : setOne) {
                if (setTwo.contains(element)) {
                    return true;
                }
            }
        }
        return false;
    }

    public List<JobPostDTO> findAllJobs() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<JobPost> jobs = jobPostRepository.findAll(pageable);
        List<JobPostDTO> jobPostDto = jobs.stream().map(this::convertToDto).collect(Collectors.toList());
        return jobPostDto;
    }

    public List<JobFilterDTO> findAllJobsbyUser(Long userId, Long consultancyId, int status, Integer pageNo, Integer pageSize) throws Exception {
        // Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("createdDate").descending());

        List<Long> users = new ArrayList<>();
        if (status == 1) {
            users = userRepository.findUsersIdsByConsultancyIdAndEnabled(consultancyId, true, userId,
                    null);
        }
        users.add(userId);
        Page<JobPost> jobs = jobPostRepository.findJobByUserIdOrCollaboratorIdAndStatus(users, JobStatus.ACTIVE, String.valueOf(userId), null);
        List<JobFilterDTO> jobPostDto = new ArrayList<JobFilterDTO>();
        for (JobPost job : jobs) {
            JobFilterDTO jobPostDTO = convertToDtoforFilter(job);
            jobPostDto.add(jobPostDTO);
        }
        return jobPostDto;
    }

    public List<JobStateResultSet> getJobCounts(long userId) {
        List<JobStateResultSet> jobsCountByStatus = jobPostRepository.getJobStatesForUser(userId);
        return jobsCountByStatus;

    }

    public JobPostDTO findJobPostById(long jobId) throws JobNotFoundException {
        try {
            JobPost jobPost = jobPostRepository.getById(jobId);
            JobPostDTO jobPostDTO = convertToDto(jobPost);
            return jobPostDTO;
        } catch (EntityNotFoundException e) {
            log.error("Get job by id failed : ", e);
            throw new JobNotFoundException("No data found");
        }
    }

    public Long saveJobPost(JobPostDTO jobPostDTO, long consultantId) throws CreateJobFailedException {
        try {
            jobPostDTO.setStatus(JobStatus.PENDING);
            JobPost jobModel = convertToEntity(jobPostDTO);
            jobModel.setCreatedDate(new Date());
            jobModel.setUpdatedDate(new Date());
            if (jobPostDTO.getJobTypeStartDate() != null)
                jobModel.setStartDate(jobPostDTO.getJobTypeStartDate());
            if (jobPostDTO.getJobTypeEndDate() != null)
                jobModel.setEndDate(jobPostDTO.getJobTypeEndDate());
            updateJobDependencydata(jobPostDTO, jobModel);
            jobModel.setHideSalary(jobPostDTO.isHideSalary());
            jobModel.setJobTypeUnpaid(jobPostDTO.isJobTypeUnpaid());
            jobPostRepository.save(jobModel);


            User user = userRepository.getById(jobPostDTO.getUserId());
            if (user.getCreatedBy() != null) {
                String message = "New " + jobModel.getTitle() + " job posted by " + user.getFirstName() + " " + user.getLastName();
                alertService.saveAlertInfo(user.getCreatedBy(), message, consultantId);
            }
            updateCollabratorsAlerts(jobPostDTO, jobModel, user.getFirstName() + " " + user.getLastName(), consultantId);

            return jobModel.getJobPostId();
        } catch (Exception e) {
            log.error("Save job failed : ", e);
            throw new CreateJobFailedException(e.getMessage());
        }
    }

    private void updateCollabratorsAlerts(JobPostDTO jobPostDTO, JobPost jobModel, String name, long consultantId) {
        String message = null;
        if (jobPostDTO.getCollaboratorId() != null && !jobPostDTO.getCollaboratorId().isEmpty()
                && jobPostDTO.getCollaboratorId().contains(",")) {

            List<Long> ids = Stream.of(jobPostDTO.getCollaboratorId().split(",")).map(Long::parseLong)
                    .collect(Collectors.toList());
            // ids.add(jobModel.getUserId());

            List<User> users = userRepository.findByConsultancyUsers(ids);
            for (User user : users) {
                message = user.getFirstName() + " " + user.getLastName() + " added as a collaborator for "
                        + jobModel.getTitle() + " job  by " + name;
                alertService.saveAlertInfo(user.getUserId(), message, consultantId);
                alertService.saveAlertInfo(jobModel.getUserId(), message, consultantId);
            }

        } else if (jobPostDTO.getCollaboratorId() != null && !jobPostDTO.getCollaboratorId().isEmpty()) {

            List<Long> ids = new ArrayList<Long>();
            ids.add(Long.valueOf(jobPostDTO.getCollaboratorId()));
            List<User> users = userRepository.findByConsultancyUsers(ids);
            // ids.add(jobModel.getUserId());
            for (User user : users) {
                message = user.getFirstName() + " " + user.getLastName() + " added as a collaborator for "
                        + jobModel.getTitle() + " job  by " + name;
                alertService.saveAlertInfo(user.getUserId(), message, consultantId);
                alertService.saveAlertInfo(jobModel.getUserId(), message, consultantId);

            }

        }
    }

    private void updateJobCloseAlerts(JobPost jobPost, long userId, long consultantId) {
        String message = null;
        if (jobPost.getCollaboratorId() != null && !jobPost.getCollaboratorId().isEmpty()
                && jobPost.getCollaboratorId().contains(",")) {

            List<Long> ids = Stream.of(jobPost.getCollaboratorId().split(",")).map(Long::parseLong)
                    .collect(Collectors.toList());
            // ids.add(jobModel.getUserId());

            List<User> users = userRepository.findByConsultancyUsers(ids);
            ids.add(jobPost.getUserId());
            User userPosted = userRepository.getById(jobPost.getUserId());
            User actionTakenBy = userRepository.getById(userId);
            message = jobPost.getTitle() + " job posted by " + userPosted.getFirstName() + " " + userPosted.getLastName() + " is now closed by " + actionTakenBy.getFirstName() + " " + actionTakenBy.getLastName();
            alertService.saveAlertInfo(actionTakenBy.getUserId(), message, actionTakenBy.getConsultancyId());
            alertService.saveAlertInfo(userPosted.getUserId(), message, consultantId);
            for (User user : users) {
                if (user.getUserId() != userPosted.getUserId() && user.getUserId() != actionTakenBy.getUserId()) {
                    alertService.saveAlertInfo(user.getUserId(), message, consultantId);
                }
            }


        } else if (jobPost.getCollaboratorId() != null && !jobPost.getCollaboratorId().isEmpty()) {

            List<Long> ids = new ArrayList<Long>();
            ids.add(Long.valueOf(jobPost.getCollaboratorId()));
            ids.add(jobPost.getUserId());
            List<User> users = userRepository.findByConsultancyUsers(ids);
            User userPosted = userRepository.getById(jobPost.getUserId());
            User actionTakenBy = userRepository.getById(userId);
            message = jobPost.getTitle() + " job posted by " + userPosted.getFirstName() + " " + userPosted.getLastName() + " is now closed by " + actionTakenBy.getFirstName() + " " + actionTakenBy.getLastName();
            alertService.saveAlertInfo(actionTakenBy.getUserId(), message, actionTakenBy.getConsultancyId());
            for (User user : users) {
                // if (user.getUserId() != userPosted.getUserId() && user.getUserId() != actionTakenBy.getUserId()) {
                alertService.saveAlertInfo(user.getUserId(), message, consultantId);
                // }
            }
        }
    }

    private void updateJobDependencydata(JobPostDTO jobPostDto, JobPost jobModel) {
        if (!jobPostDto.getJobFunction().isEmpty()) {
            JobFunction jobFunction;
            Set<JobFunction> jobFunctionSet = new HashSet<>();
            for (Integer jobFunctionId : jobPostDto.getJobFunction()) {
                jobFunction = new JobFunction();
                jobFunction.setJobFunctionId(jobFunctionId);
                jobFunctionSet.add(jobFunction);
            }
            jobModel.setJobFunction(jobFunctionSet);
            runJobMatcher = true;
        }
        if (!jobPostDto.getOptionalJobfunctions().isEmpty()) {
            JobFunction jobFunction;
            Set<JobFunction> jobFunctionSet = new HashSet<>();
            for (Integer jobFunctionId : jobPostDto.getOptionalJobfunctions()) {
                jobFunction = new JobFunction();
                jobFunction.setJobFunctionId(jobFunctionId);
                jobFunctionSet.add(jobFunction);
            }
            jobModel.setOptionalJobfunctions(jobFunctionSet);
            runJobMatcher = true;
        } else {
            Set<JobFunction> jobFunctionSet = new HashSet<>();
            jobModel.setOptionalJobfunctions(jobFunctionSet);
            runJobMatcher = true;
        }
        if (!jobPostDto.getJobSkills().isEmpty()) {
            JobSkills jobSkills;
            Set<JobSkills> jobSkillSet = new HashSet<>();
            for (Integer jobSkillId : jobPostDto.getJobSkills()) {
                jobSkills = new JobSkills();
                jobSkills.setSkillId(jobSkillId);
                jobSkillSet.add(jobSkills);
            }
            jobModel.setJobSkills(jobSkillSet);
            runJobMatcher = true;
        }

        if (!jobPostDto.getSupplementPayOther().isEmpty()) {
            jobModel.setSupplementPayOther(jobPostDto.getSupplementPayOther());
        } else {
            jobModel.setSupplementPayOther("");
        }
        if (!jobPostDto.getJobBenefits().isEmpty()) {
            Benefits jobBenefits;
            Set<Benefits> jobjobBenefitSet = new HashSet<>();
            for (Integer jobBenefitId : jobPostDto.getJobBenefits()) {
                jobBenefits = new Benefits();
                jobBenefits.setBenefitId(jobBenefitId);
                jobjobBenefitSet.add(jobBenefits);
            }
            jobModel.setJobBenefits(jobjobBenefitSet);
        } else {
            Set<Benefits> jobjobBenefitSet = new HashSet<>();
            jobModel.setJobBenefits(jobjobBenefitSet);
        }

        if (!jobPostDto.getJobSupplementalPay().isEmpty()) {
            SupplementalPay jobSupplementPay;
            Set<SupplementalPay> jobSuppPaySet = new HashSet<>();
            for (Integer jobSuppPayId : jobPostDto.getJobSupplementalPay()) {
                jobSupplementPay = new SupplementalPay();
                jobSupplementPay.setSupplementalPayId(jobSuppPayId);
                jobSuppPaySet.add(jobSupplementPay);
            }
            jobModel.setJobSupplementalPay(jobSuppPaySet);
        } else {
            Set<SupplementalPay> jobSuppPaySet = new HashSet<>();
            jobModel.setJobSupplementalPay(jobSuppPaySet);
        }

        if (!jobPostDto.getJobDegrees().isEmpty()) {
            Degrees degrees;
            Set<Degrees> jobDegreeSet = new HashSet<>();
            for (Integer jobDegreeId : jobPostDto.getJobDegrees()) {
                degrees = new Degrees();
                degrees.setDegreeId(jobDegreeId);
                jobDegreeSet.add(degrees);
            }
            jobModel.setJobDegrees(jobDegreeSet);
        } else {
            Set<Degrees> jobDegreeSet = new HashSet<>();
            jobModel.setJobDegrees(jobDegreeSet);
        }

        if (!jobPostDto.getJobQuestion().isEmpty()) {
            JobQuestion jobQuestion;
            List<JobQuestion> jobQuestionSet = new ArrayList<>();
            for (JobQuestionDTO jobQuestionDTO : jobPostDto.getJobQuestion()) {
                jobQuestion = new JobQuestion();
                jobQuestion.setQuestionId(jobQuestionDTO.getQuestionId());
                jobQuestion.setQuestionTitle(jobQuestionDTO.getQuestionTitle());
                jobQuestion.setQuestionType(jobQuestionDTO.getQuestionType());
                jobQuestion.setMandatory(jobQuestionDTO.isMandatory());
                jobQuestion.setSortId(jobQuestionDTO.getSortId());
                JobQuestionValues jobQuestionValues;
                Set<JobQuestionValues> jobQuestionValuesSet = new HashSet<>();
                for (JobQuestionValuesDTO jobQuestionValuesDTO : jobQuestionDTO.getQuestionValues()) {
                    jobQuestionValues = new JobQuestionValues();
                    jobQuestionValues.setQuestionValue(jobQuestionValuesDTO.getQuestionValue());
                    jobQuestionValues.setQuesValueId(jobQuestionValuesDTO.getQuesValueId());
                    jobQuestionValuesSet.add(jobQuestionValues);
                }
                if (jobModel.getJobQuestion() != null && jobQuestionDTO.getQuestionId() != null) {
                    Optional<JobQuestion> jobQuestionOptional = jobModel.getJobQuestion().stream()
                            .filter(e -> e.getQuestionId().equals(jobQuestionDTO.getQuestionId())).findAny();
                    if (jobQuestionOptional.isPresent()) {
                        Set<JobQuestionValues> oldjobQuestionValuesSet = jobQuestionOptional.get().getQuestionValues();
                        if (oldjobQuestionValuesSet != null) {
                            List<Long> updatedjobQuestionValuesIds = jobQuestionValuesSet.stream()
                                    .map(e -> e.getQuesValueId()).collect(Collectors.toList());
                            oldjobQuestionValuesSet
                                    .removeIf(e -> updatedjobQuestionValuesIds.contains(e.getQuesValueId()));
                            if (oldjobQuestionValuesSet.size() > 0)
                                jobQuestionValuesRepository.deleteAll(oldjobQuestionValuesSet);
                        }
                    }
                }
                jobQuestion.setQuestionValues(jobQuestionValuesSet);
                jobQuestionSet.add(jobQuestion);
            }

            Set<JobQuestion> removeJobQuestionSet = new HashSet<>();
            removeJobQuestionSet.addAll(jobModel.getJobQuestion().stream().filter(e -> e.getQuestionId() != null)
                    .collect(Collectors.toSet()));
            List<Long> updateJobQuetionset = jobQuestionSet.stream().filter(e -> e.getQuestionId() != null)
                    .collect(Collectors.toSet()).stream().map(e -> e.getQuestionId()).collect(Collectors.toList());
            removeJobQuestionSet.removeIf(e -> updateJobQuetionset.contains(e.getQuestionId()));
            for (JobQuestion removeJobQuestion : removeJobQuestionSet) {
                Set<JobQuestionValues> removeJobQuestionValues = removeJobQuestion.getQuestionValues().stream()
                        .filter(e -> e.getQuesValueId() != null).collect(Collectors.toSet());
                removeJobQuestionValues.removeIf(e -> e.getQuestionValue() == null);
                if (removeJobQuestionValues != null && removeJobQuestionValues.size() > 0) {
                    jobQuestionValuesRepository.deleteAll(removeJobQuestionValues);
                }
                jobQuestionRepository.deleteById(removeJobQuestion.getQuestionId());
            }
            jobQuestionSet.sort(Comparator.comparing(a -> a.getSortId()));
            jobModel.setJobQuestion(jobQuestionSet);
        } else {
            List<JobQuestion> jobQuestionSet = new ArrayList<>();
            Set<JobQuestion> removeJobQuestionSet = new HashSet<>();
            removeJobQuestionSet.addAll(jobModel.getJobQuestion().stream().filter(e -> e.getQuestionId() != null)
                    .collect(Collectors.toSet()));
            for (JobQuestion removeJobQuestion : removeJobQuestionSet) {
                Set<JobQuestionValues> removeJobQuestionValues = removeJobQuestion.getQuestionValues().stream()
                        .filter(e -> e.getQuesValueId() != null).collect(Collectors.toSet());
                removeJobQuestionValues.removeIf(e -> e.getQuestionValue() == null);
                if (removeJobQuestionValues != null && removeJobQuestionValues.size() > 0) {
                    jobQuestionValuesRepository.deleteAll(removeJobQuestionValues);
                }
                jobQuestionRepository.deleteById(removeJobQuestion.getQuestionId());
            }
            jobModel.setJobQuestion(jobQuestionSet);
        }
//            List<JobQuestion> arrayOfQuestions = new ArrayList<>(jobQuestionSet);
//            arrayOfQuestions.sort(Comparator.comparing(a -> a.getSortId()));
//
//            List<JobQuestion> arrayOfQuestionswithOutIds = new ArrayList<>();
//            for (JobQuestion question : arrayOfQuestions) {
//                JobQuestion questionObj = new JobQuestion();
//                if (question.getQuestionId() != null) {
//                    questionObj.setQuestionId(question.getQuestionId());
//                }
//                questionObj.setQuestionTitle(question.getQuestionTitle());
//                questionObj.setQuestionType(question.getQuestionTitle());
//                questionObj.setMandatory(question.isMandatory());
//                questionObj.setQuestionValues(question.getQuestionValues());
//                arrayOfQuestionswithOutIds.add(questionObj);
//            }
//            jobModel.setJobQuestion(arrayOfQuestionswithOutIds);


    }

    private JobFilterDTO convertToDtoforFilter(JobPost post) {
        JobFilterDTO jobPostDTO = new JobFilterDTO();

        List<Integer> jbFuncList = new ArrayList<>();
        for (JobFunction jf : post.getJobFunction()) {
            jbFuncList.add(jf.getJobFunctionId());
        }
        List<Integer> opJbFuncList = new ArrayList<>();
        for (JobFunction jf : post.getOptionalJobfunctions()) {
            opJbFuncList.add(jf.getJobFunctionId());
        }


        List<Integer> jobSkilList = new ArrayList<>();
        for (JobSkills jf : post.getJobSkills()) {
            jobSkilList.add(jf.getSkillId());
        }


        List<Integer> jobDegrees = new ArrayList<>();
        for (Degrees jf : post.getJobDegrees()) {
            jobDegrees.add(jf.getDegreeId());
        }

        jobPostDTO.setJobPostId(post.getJobPostId());
        jobPostDTO.setTitle(post.getTitle());
        jobPostDTO.setUserId(post.getUserId());
        jobPostDTO.setJobFunction(jbFuncList);
        jobPostDTO.setOptionalJobfunctions(opJbFuncList);
        jobPostDTO.setJobSkills(jobSkilList);
        jobPostDTO.setJobDegrees(jobDegrees);
        jobPostDTO.setLocation(post.getLocation());
        jobPostDTO.setExperienceMax(post.getExperienceMax());
        jobPostDTO.setExperienceMin(post.getExperienceMin());
        jobPostDTO.setCitizenship(post.getCitizenship());
        jobPostDTO.setWorklocValue(post.getWorklocValue());
        jobPostDTO.setWorklocType(post.getWorklocType());
        jobPostDTO.setNoticePeriod(post.getNoticePeriod());
        jobPostDTO.setContractNoticePeriod(post.getContractNoticePeriod());
        jobPostDTO.setJobType(post.getJobType());

        return jobPostDTO;
    }

    private JobPostDTO convertToDto(JobPost post) {

        JobPostDTO jobPostDTO = new JobPostDTO();

        ModelMapper modelMapper = new ModelMapper();
        List<Integer> jbFuncList = new ArrayList<>();
        for (JobFunction jf : post.getJobFunction()) {
            jbFuncList.add(jf.getJobFunctionId());
        }

        List<Integer> opJbFuncList = new ArrayList<>();
        for (JobFunction jf : post.getOptionalJobfunctions()) {
            opJbFuncList.add(jf.getJobFunctionId());
        }

        List<Integer> jobSkilList = new ArrayList<>();
        for (JobSkills jf : post.getJobSkills()) {
            jobSkilList.add(jf.getSkillId());
        }

        List<Integer> jobBenefits = new ArrayList<>();
        for (Benefits jf : post.getJobBenefits()) {
            jobBenefits.add(jf.getBenefitId());
        }

        List<Integer> jobDegrees = new ArrayList<>();
        for (Degrees jf : post.getJobDegrees()) {
            jobDegrees.add(jf.getDegreeId());
        }

        List<Integer> jobSupplPatList = new ArrayList<>();
        for (SupplementalPay jf : post.getJobSupplementalPay()) {
            jobSupplPatList.add(jf.getSupplementalPayId());
        }

        modelMapper.addMappings(new PropertyMap<JobPost, JobPostDTO>() {
            protected void configure() {
                map().setJobFunction(jbFuncList);
            }
        });
        modelMapper.addMappings(new PropertyMap<JobPost, JobPostDTO>() {
            protected void configure() {
                map().setOptionalJobfunctions(opJbFuncList);
            }
        });
        modelMapper.addMappings(new PropertyMap<JobPost, JobPostDTO>() {
            protected void configure() {
                map().setJobSkills(jobSkilList);
            }
        });
        modelMapper.addMappings(new PropertyMap<JobPost, JobPostDTO>() {
            protected void configure() {
                map().setJobBenefits(jobBenefits);
            }
        });
        modelMapper.addMappings(new PropertyMap<JobPost, JobPostDTO>() {
            protected void configure() {
                map().setJobSupplementalPay(jobSupplPatList);
            }
        });
        modelMapper.addMappings(new PropertyMap<JobPost, JobPostDTO>() {
            protected void configure() {
                map().setJobDegrees(jobDegrees);
            }
        });

        jobPostDTO.setCreatedDate(post.getCreatedDate());
        jobPostDTO.setUpdatedDate(post.getUpdatedDate());


        Optional<User> recruiter = userRepository.findById(post.getUserId());
        if (recruiter.isPresent())
            jobPostDTO.setJobPostedBy(recruiter.get().getFirstName() + " " + recruiter.get().getLastName());


        if (post.getCollaboratorId() != null && !post.getCollaboratorId().isEmpty()
                && post.getCollaboratorId().contains(",")) {

            List<Long> ids = Stream.of(post.getCollaboratorId().split(",")).map(Long::parseLong)
                    .collect(Collectors.toList());

            List<User> users = userRepository.findByConsultancyUsers(ids);
            List<UserDTO> usersList = new ArrayList<UserDTO>();

            for (User user : users) {
                UserDTO userDto = new UserDTO();
                userDto.setFirstName(user.getFirstName());
                userDto.setLastName(user.getLastName());
                userDto.setEmail(user.getWorkEmail());
                userDto.setUserId(user.getUserId());
                userDto.setCompanyId(user.getCompanyId());
                userDto.setConsultancyId(user.getConsultancyId());
                userDto.setPhoneNumber(user.getPhoneNumber());
                if (user.getDepartmentId() != null) {
                    userDto.setDepartment(departmentRepository.findById(user.getDepartmentId()).get().getName());
                }
                usersList.add(userDto);
            }

            jobPostDTO.setCollaboratorsData(usersList);
        } else if (post.getCollaboratorId() != null && !post.getCollaboratorId().isEmpty()) {

            List<Long> ids = new ArrayList<Long>();
            ids.add(Long.valueOf(post.getCollaboratorId()));
            List<User> users = userRepository.findByConsultancyUsers(ids);
            List<UserDTO> usersList = new ArrayList<UserDTO>();

            for (User user : users) {
                UserDTO userDto = new UserDTO();
                userDto.setFirstName(user.getFirstName());
                userDto.setLastName(user.getLastName());
                userDto.setEmail(user.getWorkEmail());
                userDto.setUserId(user.getUserId());
                userDto.setCompanyId(user.getCompanyId());
                userDto.setConsultancyId(user.getConsultancyId());
                userDto.setPhoneNumber(user.getPhoneNumber());
                if (user.getDepartmentId() != null) {
                    userDto.setDepartment(departmentRepository.findById(user.getDepartmentId()).get().getName());
                }
                usersList.add(userDto);
            }

            jobPostDTO.setCollaboratorsData(usersList);

        } else {
            List<UserDTO> usersList = new ArrayList<UserDTO>();
            jobPostDTO.setCollaboratorsData(usersList);
        }

        modelMapper.map(post, jobPostDTO);
        jobPostDTO.setJobTypeEndDate(post.getEndDate());
        jobPostDTO.setSupplementPayOther(post.getSupplementPayOther());
        jobPostDTO.setJobTypeStartDate(post.getStartDate());
        return jobPostDTO;
    }

    private JobPost convertToEntity(JobPostDTO jobPostDto) {
        JobPost post = modelMapper.map(jobPostDto, JobPost.class);
        return post;
    }

    @Transactional
    public Long updateJobPost(Long jobId, JobPostDTO jobPostDTO, String name, long consultantId, long userId) throws JobNotFoundException {

        try {
            Optional<JobPost> jobPostOpt = jobPostRepository.findById(jobId);
            if (!jobPostOpt.isPresent()) {
                throw new JobNotFoundException("No data found");
            }
            JobPost jobPost = jobPostOpt.get();

            if (StringUtils.isNullOrEmpty(jobPost.getCollaboratorId()) && !StringUtils.isNullOrEmpty(jobPostDTO.getCollaboratorId())) {
                updateCollabratorsAlerts(jobPostDTO, jobPost, name, consultantId);
            }

            if (jobPostDTO.getTitle() != null)
                jobPost.setTitle(jobPostDTO.getTitle());

            if (jobPostDTO.getStatus() != null) {
                jobPost.setStatus(jobPostDTO.getStatus());
                if (jobPostDTO.getStatus().equals(JobStatus.CLOSED)) {
                    updateJobCloseAlerts(jobPost, userId, consultantId);
                }
//                //TODO run after job activated
//                if (jobPostDTO.getStatus().equals(JobStatus.ACTIVE)) {
//                    jobPost.setUpdatedDate(new Date());
//                    jobPostRepository.save(jobPost);
//                    if (runJobMatcher) {
//                        runJobMatcherForJob(jobId);
//                        JobForQueue j = new JobForQueue();
//                        j.setJobPostId(jobId);
//                        jobQueue.put(j);
//                    }
//
//                }
//                return jobPost.getJobPostId();
            }

            if (jobPostDTO.getJobType() != null) {
                jobPost.setJobType(jobPostDTO.getJobType());
                runJobMatcher = true;
            }
            jobPost.setHideSalary(jobPostDTO.isHideSalary());
            jobPost.setJobTypeUnpaid(jobPostDTO.isJobTypeUnpaid());

            if (jobPostDTO.getWorklocType() != null)
                jobPost.setWorklocType(jobPostDTO.getWorklocType());
            // if (jobPostDTO.getActivatedDate() != null)
            // jobPost.setActivatedDate(jobPostDTO.getActivatedDate());
            if (jobPostDTO.getBenefitOther() != null) {
                jobPost.setBenefitOther(jobPostDTO.getBenefitOther());
            } else {
                jobPost.setBenefitOther(null);
            }

            if (jobPostDTO.getCertOrLicenseReq() != null) {
                jobPost.setCertOrLicenseReq(jobPostDTO.getCertOrLicenseReq());
            } else {
                jobPost.setCertOrLicenseReq(null);
            }
            if (jobPostDTO.getCitizenship() != null)
                jobPost.setCitizenship(jobPostDTO.getCitizenship());
            if (jobPostDTO.getCollaboratorId() != null)
                jobPost.setCollaboratorId(jobPostDTO.getCollaboratorId());
            if (jobPostDTO.getContractNoticePeriod() != null)
                jobPost.setContractNoticePeriod(jobPostDTO.getContractNoticePeriod());
            if (jobPostDTO.getCurrency() != null)
                jobPost.setCurrency(jobPostDTO.getCurrency());
            if (jobPostDTO.getDescription() != null)
                jobPost.setDescription(jobPostDTO.getDescription());
            if (jobPostDTO.getEnableViewFor() != null)
                jobPost.setEnableViewFor(jobPostDTO.getEnableViewFor());
            jobPost.setStartDate(jobPostDTO.getJobTypeStartDate() != null ? jobPostDTO.getJobTypeStartDate() : null);
            jobPost.setEndDate(jobPostDTO.getJobTypeEndDate() != null ? jobPostDTO.getJobTypeEndDate() : null);
            if (jobPostDTO.getEthnicity() != null)
                jobPost.setEthnicity(jobPostDTO.getEthnicity());
            if (jobPostDTO.getExperienceMax() != null) {
                jobPost.setExperienceMax(jobPostDTO.getExperienceMax());
                runJobMatcher = true;
            }
            if (jobPostDTO.getExperienceMin() != null) {
                jobPost.setExperienceMin(jobPostDTO.getExperienceMin());
                runJobMatcher = true;
            }
            if (jobPostDTO.getJobApplyBy() != null) {
                jobPost.setJobApplyBy(jobPostDTO.getJobApplyBy());
            } else {
                jobPost.setJobApplyBy(null);
            }

            if (jobPostDTO.getJobTypeDuration() != null)
                jobPost.setJobTypeDuration(jobPostDTO.getJobTypeDuration());
            if (jobPostDTO.getLocation() != null) {
                jobPost.setLocation(jobPostDTO.getLocation());
                runJobMatcher = true;
            }
            if (jobPostDTO.getNoticePeriod() != null)
                jobPost.setNoticePeriod(jobPostDTO.getNoticePeriod());
            if (jobPostDTO.getSalaryMax() != null)
                jobPost.setSalaryMax(jobPostDTO.getSalaryMax());
            if (jobPostDTO.getSalaryMin() != null)
                jobPost.setSalaryMin(jobPostDTO.getSalaryMin());
            if (jobPostDTO.getSalaryType() != null)
                jobPost.setSalaryType(jobPostDTO.getSalaryType());
            if (jobPostDTO.getVacancies() != null)
                jobPost.setVacancies(jobPostDTO.getVacancies());
            if (jobPostDTO.getWorklocValue() != null)
                jobPost.setWorklocValue(jobPostDTO.getWorklocValue());

            jobPost.setUpdatedDate(new Date());
            updateJobDependencydata(jobPostDTO, jobPost);
            jobPostRepository.save(jobPost);
            if (runJobMatcher) {
                runJobMatcherForUpdatedJob(jobId, consultantId);
//                JobForQueue j = new JobForQueue();
//                j.setJobPostId(jobId);
//                jobQueue.put(j);
            }
            return jobPost.getJobPostId();
        } catch (JobNotFoundException e) {
            e.printStackTrace();
            throw new JobNotFoundException(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jobId;
    }

    public Long deleteJobById(long jobId) throws JobNotFoundException {
        Optional<JobPost> jobPost = Optional.ofNullable(jobPostRepository.findById(jobId)
                .orElseThrow(() -> new JobNotFoundException("Job post nod found with id : " + jobId)));
        jobPostRepository.deleteById(jobId);
        return jobId;
    }

    public void deleteAllJobs() {
        jobPostRepository.deleteAll();
    }

    public JobViewResponse getAllJobsForUser(long userId, JobStatus status, int filter, Integer pageNo, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("createdDate").descending());

        try {
            // Fetch jobs first by pagination
            Page<JobPost> jobPostPage = null;
            if (filter == 0) {
                jobPostPage = jobPostRepository.findJobByUserIdAndStatus(userId, pageable);
            } else {
                jobPostPage = jobPostRepository.findJobByUserIdOrCollaboratorIdAndStatus(userId, String.valueOf(userId),
                        pageable);
            }
            JobViewResponse res = getJobInfo(jobPostPage, userId);
            return res;
        } catch (Exception e) {
            log.error("Exception : ", e);
        }

        return null;

    }

    private JobViewResponse getJobInfo(Page<JobPost> jobPostPage, Long userId) {
        try {
            List<JobViewDTO> jobViewDTO = new ArrayList<>();
            List<Long> jobPostIdList = new ArrayList<>();
            JobViewDTO jobView = null;
            for (JobPost jobPost : jobPostPage.getContent()) {
                jobView = new JobViewDTO();
                jobView.setJobPostId(jobPost.getJobPostId());
                jobPostIdList.add(jobPost.getJobPostId());
                jobView.setTitle(jobPost.getTitle());
                jobView.setJobType(jobPost.getJobType());
                jobView.setPostedBy("" + jobPost.getUserId());
                User userData = userRepository.getById(jobPost.getUserId());
                jobView.setUserName(userData.getFirstName() + " " + userData.getLastName());
                jobView.setJobFunctionName(jobPost.getJobFunction().stream()
                        .map(a -> String.valueOf(a.getJobFunctionName())).collect(Collectors.joining(",")));
                jobView.setPostedOn(jobPost.getUpdatedDate());


                int shortlistedCount = 0;
                int interviewedCount = 0;
                int hiredCount = 0;
                int rejectedCount = 0;
                List<JobMatchStateResultSet> statsList = jobMatchingRepository.getStats(jobPost.getJobPostId());
                Long matchedCount = jobMatchingRepository.geJobMatchedStats(jobPost.getJobPostId());

                for (JobMatchStateResultSet jobMatchConsultancy : statsList) {
                    //jobMatchConsultancy.getHiredStatus() is user job action
                    if (jobMatchConsultancy.getInterviewStatus() == 1 && jobMatchConsultancy.getHiredStatus() == 0) {
                        shortlistedCount++;
                    } else if (jobMatchConsultancy.getInterviewStatus() == 2 || jobMatchConsultancy.getInterviewStatus() == 3
                            || jobMatchConsultancy.getInterviewStatus() == 4 || jobMatchConsultancy.getInterviewStatus() == 5
                            || jobMatchConsultancy.getInterviewStatus() == 6 || jobMatchConsultancy.getInterviewStatus() == 9
                            || jobMatchConsultancy.getInterviewStatus() == 12) {
                        interviewedCount++;
                    } else if (jobMatchConsultancy.getInterviewStatus() == 7) {
                        hiredCount++;
                    } else if (jobMatchConsultancy.getInterviewStatus() == 8 || jobMatchConsultancy.getInterviewStatus() == 10
                            || jobMatchConsultancy.getInterviewStatus() == 11 || jobMatchConsultancy.getInterviewStatus() == 18 || jobMatchConsultancy.getInterviewStatus() == 15) {
                        rejectedCount++;
                    }
                }
                jobView.setShortlisted(shortlistedCount + matchedCount);
                jobView.setInterviewed(interviewedCount);
                jobView.setHired(hiredCount);
                jobView.setRejected(rejectedCount);
                jobView.setMatchedCount(matchedCount);

                jobViewDTO.add(jobView);
            }
            jobPostIdList.clear();
            JobViewResponse res = new JobViewResponse();
            res.setTotalCount(jobPostPage.getTotalElements());
            res.setTotalPages(jobPostPage.getTotalPages());
            res.setJobViewDTO(jobViewDTO);
            // Fetch analytics info pending/active/closed
            List<JobStateResultSet> jobsCountByStatus = jobPostRepository.getJobStatesForUser(userId);
            for (JobStateResultSet jobStateResultSet : jobsCountByStatus) {
                if (JobStatus.ACTIVE.name().equals(jobStateResultSet.getJobState())) {
                    res.setActiveCount(jobStateResultSet.getJobStateCount());
                } else if (JobStatus.PENDING.name().equals(jobStateResultSet.getJobState())) {
                    res.setPendingCount(jobStateResultSet.getJobStateCount());
                } else {
                    res.setClosedCount(jobStateResultSet.getJobStateCount());
                }

            }
            return res;
        } catch (Exception e) {
            log.error("Exception : ", e);
        }
        return null;

    }

    private List<Long> getUserIdsByFilter(int filter, long recruiterId, long jobId) {
        List<Long> userIds = new ArrayList<>();
        if (filter == 0) {
            userIds.add(recruiterId);
        } else if (filter == 1 || filter == 2) {
            Optional<JobPost> jobPost = jobPostRepository.findById(jobId);
            if (jobPost.isPresent()) {
                String collaboratorId = jobPost.get().getCollaboratorId();
                if (collaboratorId != null && !collaboratorId.isEmpty()) {
                    userIds.addAll(Arrays.stream(collaboratorId.split(","))
                            .map(Long::parseLong)
                            .collect(Collectors.toList()));
                }
                userIds.remove(recruiterId);
                userIds.add(jobPost.get().getUserId());
                User user = userRepository.getById(recruiterId);
                if (user.getCreatedBy() != null) {
                    userIds.add(user.getCreatedBy());
                }
            }
            if (filter == 2) {
                userIds.add(recruiterId);
            }
        }

        return userIds;
    }

    public JobViewResponse getAllTeamPostedJobsForUser(long userId, long consultancyId, JobStatus status, int filter,
                                                       Integer pageNo, String role, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);

        try {
            Page<JobPost> jobPostPage = null;
            List<Long> users = new ArrayList<>();
            if (role.equals("COMPANY_ADMIN")) {
                users.addAll(activityService.getUserIds(consultancyId, userId, 0, role, filter));
                jobPostPage = jobPostRepository.findJobByUserIdOrCollaboratorIdAndStatus(users, pageable);
            } else {
                if (filter == 0) {
                    users.add(userId);
                    jobPostPage = jobPostRepository.findJobByUserIdOrCollaboratorIdAndStatus(users, pageable);
                } else if (filter == 1) {
                    jobPostPage = jobPostRepository.findJobByCollaboratorIdAndStatus(String.valueOf(userId), pageable);
                } else {
                    jobPostPage = jobPostRepository.findJobByUserIdOrCollaboratorIdAndStatus(userId, String.valueOf(userId), pageable);
                }
            }
            List<JobViewDTO> jobViewDTO = new ArrayList<>();
            List<Long> jobPostIdList = new ArrayList<>();
            JobViewDTO jobView = null;
            for (JobPost jobPost : jobPostPage.getContent()) {
                jobView = new JobViewDTO();
                jobView.setJobPostId(jobPost.getJobPostId());
                jobPostIdList.add(jobPost.getJobPostId());
                jobView.setTitle(jobPost.getTitle());
                jobView.setJobStatus(jobPost.getStatus().toString());
                jobView.setJobType(jobPost.getJobType());
                jobView.setPostedBy("" + jobPost.getUserId());
                User userData = userRepository.getById(jobPost.getUserId());
                jobView.setUserName(userData.getFirstName() + " " + userData.getLastName());
                jobView.setJobFunctionName(jobPost.getJobFunction().stream()
                        .map(a -> String.valueOf(a.getJobFunctionName())).collect(Collectors.joining(",")));
                jobView.setPostedOn(jobPost.getUpdatedDate());


                List<JobMatchStateResultSet> statsList = jobMatchingRepository.getStats(jobPost.getJobPostId());
                Long matchedCount = jobMatchingRepository.geJobMatchedStats(jobPost.getJobPostId());
                List<Date> updatedDate = jobMatchConsultancyRepository.geJobMatchedUpdatedDate(jobPost.getJobPostId());
                if (updatedDate != null && updatedDate.size() > 0)
                    jobView.setLastActionDate(updatedDate.get(0));
                else
                    jobView.setLastActionDate(jobPost.getUpdatedDate());
                int shortlistedCount = 0;
                int interviewedCount = 0;
                int hiredCount = 0;
                int rejectedCount = 0;

                for (JobMatchStateResultSet jobMatchConsultancy : statsList) {
                    if (jobMatchConsultancy.getInterviewStatus() == 1 && jobMatchConsultancy.getHiredStatus() == 0) {
                        shortlistedCount++;
                    } else if (jobMatchConsultancy.getInterviewStatus() == 2 || jobMatchConsultancy.getInterviewStatus() == 3
                            || jobMatchConsultancy.getInterviewStatus() == 4 || jobMatchConsultancy.getInterviewStatus() == 5
                            || jobMatchConsultancy.getInterviewStatus() == 6 || jobMatchConsultancy.getInterviewStatus() == 9 || jobMatchConsultancy.getInterviewStatus() == 12
                    ) {
                        interviewedCount++;
                    } else if (jobMatchConsultancy.getInterviewStatus() == 7) {
                        hiredCount++;
                    } else if (jobMatchConsultancy.getInterviewStatus() == 8 || jobMatchConsultancy.getInterviewStatus() == 10
                            || jobMatchConsultancy.getInterviewStatus() == 11 || jobMatchConsultancy.getInterviewStatus() == 18 || jobMatchConsultancy.getInterviewStatus() == 15) {
                        rejectedCount++;
                    }
                }

                jobView.setShortlisted(shortlistedCount + matchedCount);
                jobView.setInterviewed(interviewedCount);
                jobView.setHired(hiredCount);
                jobView.setRejected(rejectedCount);
                jobView.setMatchedCount(matchedCount);

                jobViewDTO.add(jobView);
            }
            jobViewDTO.sort(Comparator.comparing(JobViewDTO::getLastActionDate).reversed());
            JobViewResponse res = new JobViewResponse();
            res.setTotalCount(jobPostPage.getTotalElements());
            res.setTotalPages(jobPostPage.getTotalPages());
            res.setJobViewDTO(jobViewDTO);
            // Fetch analytics info pending/active/closed
            List<JobStateResultSet> jobsCountByStatus = jobPostRepository.getJobStatesForUser(userId);
            for (JobStateResultSet jobStateResultSet : jobsCountByStatus) {
                if (JobStatus.ACTIVE.name().equals(jobStateResultSet.getJobState())) {
                    res.setActiveCount(jobStateResultSet.getJobStateCount());
                } else if (JobStatus.PENDING.name().equals(jobStateResultSet.getJobState())) {
                    res.setPendingCount(jobStateResultSet.getJobStateCount());
                } else {
                    res.setClosedCount(jobStateResultSet.getJobStateCount());
                }

            }
            return res;
        } catch (Exception e) {
            log.error("Exception : ", e);
        }

        return null;
    }

    public JobViewResponse getDashboardAllPostedJobsForUserIncludingTeam(long userId, long consultancyId, String role, JobStatus status, int filterStatus,
                                                                         Integer pageNo, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("createdDate").descending());

//        if (JobStatus.CLOSED == status) {
//            pageable = PageRequest.of(pageNo, pageSize, Sort.by("updatedDate").descending());
//        }

        List<Long> users = new ArrayList<>();
        if (role.equals("COMPANY_ADMIN")) {
            users.addAll(activityService.getUserIds(consultancyId, userId, 0, role, filterStatus));
        } else {
            users.add(userId);
        }
        JobViewResponse jobViewResponse = getAllPostedJobsForUserIncludingTeam(users, userId, filterStatus, status, role,
                pageable);
        return jobViewResponse;
    }

    public JobViewResponse getAllPostedJobsForUserIncludingTeam(List<Long> users, long userId, int filterStatus, JobStatus status, String role,
                                                                Pageable pageable) {
        try {
            Page<JobPost> jobPostPage = null;
            Long activeCount = 0L;
            Long pendingCount = 0L;
            Long closeCount = 0L;

            if (filterStatus == 0 || role.equals("COMPANY_ADMIN")) {
                jobPostPage = jobPostRepository.findJobByUserIdOrCollaboratorIdAndStatus(users, status,
                        pageable);
                activeCount = jobPostRepository.findJobPostByJobPostIdAndStatus(users, JobStatus.ACTIVE);
                pendingCount = jobPostRepository.findJobPostByJobPostIdAndStatus(users, JobStatus.PENDING);
                closeCount = jobPostRepository.findJobPostByJobPostIdAndStatus(users, JobStatus.CLOSED);
            } else {
                // Fetch jobs first by pagination
                if (filterStatus == 1) {
                    jobPostPage = jobPostRepository.findJobByCollaboratorIdAndStatus(String.valueOf(userId), status, pageable);
                    activeCount = jobPostRepository.findJobPostByJobPostIdAndStatus(String.valueOf(userId), JobStatus.ACTIVE);
                    pendingCount = jobPostRepository.findJobPostByJobPostIdAndStatus(String.valueOf(userId), JobStatus.PENDING);
                    closeCount = jobPostRepository.findJobPostByJobPostIdAndStatus(String.valueOf(userId), JobStatus.CLOSED);

                } else {
                    jobPostPage = jobPostRepository.findJobByUserIdOrCollaboratorIdAndStatus(users, String.valueOf(userId), status,
                            pageable);
                    activeCount = jobPostRepository.findJobPostByJobPostIdAndStatus(users, String.valueOf(userId), JobStatus.ACTIVE);
                    pendingCount = jobPostRepository.findJobPostByJobPostIdAndStatus(users, String.valueOf(userId), JobStatus.PENDING);
                    closeCount = jobPostRepository.findJobPostByJobPostIdAndStatus(users, String.valueOf(userId), JobStatus.CLOSED);
                }
            }
            List<JobViewDTO> jobViewDTO = new ArrayList<>();
            List<Long> jobPostIdList = new ArrayList<>();
            JobViewDTO jobView = null;
            for (JobPost jobPost : jobPostPage.getContent()) {
                jobView = new JobViewDTO();
                jobView.setJobPostId(jobPost.getJobPostId());
                jobPostIdList.add(jobPost.getJobPostId());
                jobView.setTitle(jobPost.getTitle());
                jobView.setJobType(jobPost.getJobType());
                jobView.setPostedBy("" + jobPost.getUserId());
                User userData = userRepository.getById(jobPost.getUserId());
                jobView.setUserName(userData.getFirstName() + " " + userData.getLastName());
                jobView.setJobFunctionName(jobPost.getJobFunction().stream()
                        .map(a -> String.valueOf(a.getJobFunctionName())).collect(Collectors.joining(",")));
                jobView.setPostedOn(jobPost.getCreatedDate());
                if (jobPost.getCollaboratorId() != null && !jobPost.getCollaboratorId().isEmpty()
                        && jobPost.getCollaboratorId().contains(",")) {

                    List<Long> ids = Stream.of(jobPost.getCollaboratorId().split(",")).map(Long::parseLong)
                            .collect(Collectors.toList());

                    List<User> usersCollab = userRepository.findByConsultancyUsers(ids);
                    List<UserDTO> usersList = new ArrayList<UserDTO>();

                    for (User user : usersCollab) {
                        UserDTO userDto = new UserDTO();
                        userDto.setFirstName(user.getFirstName());
                        userDto.setLastName(user.getLastName());
                        userDto.setEmail(user.getWorkEmail());
                        userDto.setUserId(user.getUserId());
                        userDto.setCompanyId(user.getCompanyId());
                        userDto.setConsultancyId(user.getConsultancyId());
                        userDto.setPhoneNumber(user.getPhoneNumber());
                        usersList.add(userDto);
                    }

                    jobView.setUsersList(usersList);
                } else if (jobPost.getCollaboratorId() != null && !jobPost.getCollaboratorId().isEmpty()) {

                    List<Long> ids = new ArrayList<Long>();
                    ids.add(Long.valueOf(jobPost.getCollaboratorId()));
                    List<User> usersCollab = userRepository.findByConsultancyUsers(ids);
                    List<UserDTO> usersList = new ArrayList<UserDTO>();

                    for (User user : usersCollab) {
                        UserDTO userDto = new UserDTO();
                        userDto.setFirstName(user.getFirstName());
                        userDto.setLastName(user.getLastName());
                        userDto.setEmail(user.getWorkEmail());
                        userDto.setUserId(user.getUserId());
                        userDto.setCompanyId(user.getCompanyId());
                        userDto.setConsultancyId(user.getConsultancyId());
                        userDto.setPhoneNumber(user.getPhoneNumber());
                        usersList.add(userDto);
                    }

                    jobView.setUsersList(usersList);

                } else {
                    List<UserDTO> usersList = new ArrayList<UserDTO>();
                    jobView.setUsersList(usersList);
                }
                List<JobMatchStateResultSet> statsList = jobMatchingRepository.getStats(jobPost.getJobPostId());

                Long matchedCount = jobMatchingRepository.geJobMatchedStats(jobPost.getJobPostId());
                int shortlistedCount = 0;
                int interviewedCount = 0;
                int hiredCount = 0;
                int rejectedCount = 0;

                for (JobMatchStateResultSet jobMatchConsultancy : statsList) {
                    //jobMatchConsultancy.getHiredStatus() is user job action
                    if (jobMatchConsultancy.getInterviewStatus() == 1 && jobMatchConsultancy.getHiredStatus() == 0) {
                        shortlistedCount++;
                    } else if (jobMatchConsultancy.getInterviewStatus() == 2 || jobMatchConsultancy.getInterviewStatus() == 3
                            || jobMatchConsultancy.getInterviewStatus() == 4 || jobMatchConsultancy.getInterviewStatus() == 5
                            || jobMatchConsultancy.getInterviewStatus() == 6 || jobMatchConsultancy.getInterviewStatus() == 9 || jobMatchConsultancy.getInterviewStatus() == 12) {
                        interviewedCount++;
                    } else if (jobMatchConsultancy.getInterviewStatus() == 7) {
                        hiredCount++;
                    } else if (jobMatchConsultancy.getInterviewStatus() == 8 || jobMatchConsultancy.getInterviewStatus() == 10
                            || jobMatchConsultancy.getInterviewStatus() == 11 || jobMatchConsultancy.getInterviewStatus() == 18 || jobMatchConsultancy.getInterviewStatus() == 15) {
                        rejectedCount++;
                    }
                }
                jobView.setShortlisted(shortlistedCount + matchedCount);
                jobView.setInterviewed(interviewedCount);
                jobView.setHired(hiredCount);
                jobView.setRejected(rejectedCount);
                jobView.setMatchedCount(matchedCount);
                jobViewDTO.add(jobView);
            }
            JobViewResponse res = new JobViewResponse();
            res.setTotalCount(jobPostPage.getTotalElements());
            res.setTotalPages(jobPostPage.getTotalPages());
            res.setJobViewDTO(jobViewDTO);
            // Fetch analytics info pending/active/closed

            res.setTotalCount(jobPostPage.getTotalElements());
            res.setTotalPages(jobPostPage.getTotalPages());
            res.setActiveCount(activeCount);
            res.setPendingCount(pendingCount);
            res.setClosedCount(closeCount);


            return res;
        } catch (Exception e) {
            log.error("Exception : ", e);
        }

        return null;
    }

    public JobViewResponse getDashbaordStats(List<Long> userIds, List<Long> jobPostIdList) {
        try {
            List<JobViewDTO> jobViewDTO = new ArrayList<>();

            // Fetch the job statistics
            List<JobStatusResultSet> jobStats = jobPostRepository.getJobStatistics(jobPostIdList);
            Map<Long, JobStatisticsDTO> statsMap = new HashMap<>();
            // 1--->Shortlisted
            // 2--->Interviewed
            // 3--->Hired
            // 4--->Rejected
            JobStatisticsDTO jobStatsDTO = null;
            for (JobStatusResultSet jobStatus : jobStats) {
                jobStatsDTO = statsMap.get(jobStatus.getJobPostId()) == null ? new JobStatisticsDTO()
                        : statsMap.get(jobStatus.getJobPostId());
                jobStatsDTO.setJobPostId(jobStatus.getJobPostId());
                if (jobStatus.getInterviewStatus() == 1) {
                    jobStatsDTO.setShortlisted(jobStatus.getTotalCount());
                } else if (jobStatus.getInterviewStatus() == 2 || jobStatus.getInterviewStatus() == 3
                        || jobStatus.getInterviewStatus() == 4 || jobStatus.getInterviewStatus() == 5
                        || jobStatus.getInterviewStatus() == 6 || jobStatus.getInterviewStatus() == 9) {
                    jobStatsDTO.setInterviewed(jobStatus.getTotalCount());
                } else if (jobStatus.getInterviewStatus() == 7) {
                    jobStatsDTO.setHired(jobStatus.getTotalCount());
                } else if (jobStatus.getInterviewStatus() == 8 || jobStatus.getInterviewStatus() == 10
                        || jobStatus.getInterviewStatus() == 11) {
                    jobStatsDTO.setRejected(jobStatus.getTotalCount());
                }

                statsMap.put(jobStatsDTO.getJobPostId(), jobStatsDTO);
            }

            for (JobViewDTO jobViewObj : jobViewDTO) {
                JobStatisticsDTO jobStat = statsMap.get(jobViewObj.getJobPostId());
                if (jobStat != null) {
                    jobViewObj.setShortlisted(jobStat.getShortlisted());
                    jobViewObj.setInterviewed(jobStat.getInterviewed());
                    jobViewObj.setHired(jobStat.getHired());
                    jobViewObj.setRejected(jobStat.getRejected());
                }

            }

            JobViewResponse res = new JobViewResponse();
            res.setJobViewDTO(jobViewDTO);
            // Fetch analytics info pending/active/closed
            List<JobStateResultSet> jobsCountByStatus = jobPostRepository.getJobStatesForUserByJobid(jobPostIdList);
            for (JobStateResultSet jobStateResultSet : jobsCountByStatus) {
                if (JobStatus.ACTIVE.name().equals(jobStateResultSet.getJobState())) {
                    res.setActiveCount(jobStateResultSet.getJobStateCount());
                } else if (JobStatus.PENDING.name().equals(jobStateResultSet.getJobState())) {
                    res.setPendingCount(jobStateResultSet.getJobStateCount());
                } else {
                    res.setClosedCount(jobStateResultSet.getJobStateCount());
                }

            }
            return res;
        } catch (Exception e) {
            log.error("Exception : ", e);
        }

        return null;
    }

    public JobViewResponse getUserJobsCount(long userId) {

        try {
            JobViewResponse res = new JobViewResponse();
            // Fetch analytics info pending/active/closed
            List<JobStateResultSet> jobsCountByStatus = jobPostRepository.getJobStatesForUser(userId);
            for (JobStateResultSet jobStateResultSet : jobsCountByStatus) {
                if (JobStatus.ACTIVE.name().equals(jobStateResultSet.getJobState())) {
                    res.setActiveCount(jobStateResultSet.getJobStateCount());
                } else if (JobStatus.PENDING.name().equals(jobStateResultSet.getJobState())) {
                    res.setPendingCount(jobStateResultSet.getJobStateCount());
                } else {
                    res.setClosedCount(jobStateResultSet.getJobStateCount());
                }

            }
            return res;
        } catch (Exception e) {
            log.error("Exception : ", e);
        }

        return null;
    }

    //After jobpost
    @Transactional
    public void runJobMatcherForJob(Long jobID, long consultantId) {
        // status = active and
        JobPost jobPost = jobPostRepository.findJobPostByJobPostIdAndStatus(jobID, JobStatus.ACTIVE);
        // 2. fetch skills, exp, etx relevant for matching
        saveJobMatchDataInDB(jobPost, consultantId);
    }

    //Jobupdate
    @Transactional
    public void runJobMatcherForUpdatedJob(Long jobID, long consultantId) {
        // status = active and
        JobPost jobPost = jobPostRepository.findJobPostByJobPostIdAndStatus(jobID, JobStatus.ACTIVE);
        // Deleting the exsisting records to avoid not matched profiles and we update with new records
        List<JobMatchConsultancy> jobMatchConsultancies = jobMatchConsultancyRepository.findByJobPostIdWithNoAction(jobPost.getJobPostId());
        jobMatchConsultancyRepository.deleteAll(jobMatchConsultancies);

        // 2. fetch skills, exp, etx relevant for matching
        saveJobMatchDataInDB(jobPost, consultantId);
    }

    // runs 6hrs duration
    @Transactional
    public void runJobMatcher(int batch) {
//        int pageNo = 0;
//        int totalPages = 0;
//        do {
//            int pageSize = batch;
//            Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("updatedDate").descending());
//            // status = active and
//            Page<JobPost> jobPostList = jobPostRepository.findJobPostByStatus(JobStatus.ACTIVE, pageable);
//            totalPages = jobPostList.getTotalPages();
//            // 2. fetch skills, exp, etx relevant for matching
//            jobPostList.forEach(jobPost -> saveJobMatchDataInDB(jobPost));
//
//            pageNo++;
//        } while (pageNo < totalPages);

//


    }


    @Transactional
    public void runJobMatcherForJobPost(Long jobPostId, long consultantId, int pageSize) throws Exception {
        try {
            // 1. get jobs in batch order by jobpost id
            int pageNo = 0;
            //TODO  need to check this pagesize
            Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("applicantId").descending());
            // status = active and
            JobPost jobPost = jobPostRepository.findJobPostByJobPostIdAndStatus(jobPostId, JobStatus.ACTIVE);
            // 2. fetch skills, exp, etx relevant for matching
            saveJobMatchDataInDB(jobPost, consultantId);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }

    }

    public void saveJobMatchDataInDB(JobPost jobPost, long consultantId) {
        List<JobMatch> jobMatchData = new ArrayList<>();
        List<Integer> skillList = jobPost.getJobSkills().stream().map(JobSkills::getSkillId)
                .collect(Collectors.toList());
        long expMin = jobPost.getExperienceMin() - 1;
        long expMax = jobPost.getExperienceMax() + 1;
        String location = jobPost.getLocation();
        List<Integer> jobFuncList = jobPost.getJobFunction().stream().map(JobFunction::getJobFunctionId)
                .collect(Collectors.toList());
        Integer jobType = jobPost.getJobType();
        List<Integer> optJobFuncList = jobPost.getOptionalJobfunctions() != null ? jobPost.getOptionalJobfunctions()
                .stream().map(JobFunction::getJobFunctionId).collect(Collectors.toList()) : null;
        Set<Degrees> degrees = jobPost.getJobDegrees();
        List<Integer> degreesList = degrees != null
                ? degrees.stream().map(Degrees::getDegreeId).collect(Collectors.toList())
                : null;
        // 3. get matching users for job
        List<UserInfoResultSet> matchedProfiles = jobPostRepository.findUserProfilesByCriteria(skillList,
                jobFuncList, optJobFuncList, expMin, expMax, consultantId, null);
        // 4. seperate users from consultancy to other

        Set<Long> normalApplicants = matchedProfiles.stream().filter(e -> (e.getConsultancyId() == null || e.getConsultancyId() == 0)).map(e -> e.getApplicantId()).collect(Collectors.toSet());
        Map<Long, Set<Long>> consultancyMatchedConsultancyUserMap = matchedProfiles.stream().filter(e -> e.getConsultancyId() != null && e.getConsultancyUserId() != null)
                .collect(Collectors.groupingBy(
                        UserInfoResultSet::getConsultancyId,
                        Collectors.mapping(UserInfoResultSet::getConsultancyUserId, Collectors.toSet())
                ));
        Map<Long, Set<Long>> consultancyUserMatchedConsultancyApplicantMap = matchedProfiles.stream().filter(e -> e.getConsultancyId() != null && e.getConsultancyUserId() != null)
                .collect(Collectors.groupingBy(
                        UserInfoResultSet::getConsultancyUserId,
                        Collectors.mapping(UserInfoResultSet::getApplicantId, Collectors.toSet())
                ));

        Set<Long> allApplicantIds = new HashSet<>();
        if (consultancyUserMatchedConsultancyApplicantMap != null && consultancyUserMatchedConsultancyApplicantMap.size() > 0) {
            allApplicantIds.addAll(consultancyUserMatchedConsultancyApplicantMap.values().stream()
                    .flatMap(Collection::stream)
                    .collect(Collectors.toSet()));
        }
        if (normalApplicants != null && normalApplicants.size() > 0)
            allApplicantIds.addAll(normalApplicants);

//        if (applicantIds != null && applicantIds.size() > 0)
//            allApplicantIds.addAll(applicantIds);

        List<Applicant> applicants = new ArrayList<>();
        Map<Long, Applicant> applicantMap = new HashMap<>();
        Map<Long, ApplicantDetails> applicantDetailsMap = new HashMap<>();
        Map<Long, Set<Integer>> applicantJobSkillMap = new HashMap<>();
        Map<Long, Set<Integer>> applicantJobFunctionMap = new HashMap<>();
        Map<Long, Set<Integer>> applicantSecondaryJobFunctionMap = new HashMap<>();
        Map<Long, Set<Integer>> applicantDegreeMap = new HashMap<>();
        if (allApplicantIds.size() > 0) {
            applicants = (List<Applicant>) applicantRepository.findAllById(allApplicantIds);
            applicantMap = applicants.stream()
                    .collect(Collectors.toMap(Applicant::getApplicantId, Function.identity()));
            //get all applicant Details
            applicantDetailsMap = applicantDetailsRepository
                    .findAllByApplicantIdIn(allApplicantIds)
                    .stream()
                    .collect(Collectors.toMap(ApplicantDetails::getApplicantId, Function.identity()));

            //get all applicant job functions
            applicantJobFunctionMap = applicantJobFunctionRepository
                    .findAllByApplicantIdIn(allApplicantIds)
                    .stream()
                    .collect(Collectors.groupingBy(
                            ApplicantJobFunction::getApplicantId,
                            Collectors.mapping(ApplicantJobFunction::getJobFunctionId, Collectors.toSet())
                    ));

            //get all applicant job skills
            applicantJobSkillMap = applicantJobSkillRepository
                    .findAllByApplicantIdIn(allApplicantIds)
                    .stream()
                    .collect(Collectors.groupingBy(
                            ApplicantJobSkill::getApplicantId,
                            Collectors.mapping(ApplicantJobSkill::getJobSkillId, Collectors.toSet())
                    ));

            applicantSecondaryJobFunctionMap = applicantSecondaryJobFunctionRepository
                    .findAllByApplicantIdIn(allApplicantIds)
                    .stream()
                    .collect(Collectors.groupingBy(
                            ApplicantSecondaryJobFunction::getApplicantId,
                            Collectors.mapping(ApplicantSecondaryJobFunction::getJobFunctionId, Collectors.toSet())
                    ));
            applicantDegreeMap = educationHistoryRepository.findAllByApplicantIdIn(allApplicantIds)
                    .stream()
                    .collect(Collectors.groupingBy(
                            EducationHistory::getApplicantId,
                            Collectors.mapping(
                                    educationHistory -> {
                                        Degrees degree = degreesRepository.findByShortTitle(educationHistory.getDegree());
                                        return (degree != null) ? degree.getDegreeId() : null;
                                    },
                                    Collectors.toSet()
                            )
                    ));
        }
        // 5. Treat all consultancy's as user after match copy all consultancys to
        // normal applicants list
        // Insert all applicants
        for (Long applicantId : normalApplicants) {
            JobMatch jobMatchObj = new JobMatch();
            jobMatchObj.setJobPostId(jobPost.getJobPostId());
            jobMatchObj.setRecruiterId(jobPost.getUserId());
            jobMatchObj.setApplicantId(applicantId);
            jobMatchObj.setCreatedDate(new Date());
            jobMatchObj.setUpdatedDate(new Date());
            jobMatchObj.setMatchScore(calculateMatchScore(jobPost, applicantMap.get(applicantId)
                    , applicantDetailsMap.get(applicantId), applicantJobFunctionMap.get(applicantId),
                    applicantJobSkillMap.get(applicantId), applicantSecondaryJobFunctionMap.get(applicantId), applicantDegreeMap.get(applicantId)));
            jobMatchObj.setConsultancy(false);
            jobMatchData.add(jobMatchObj);
        }
        List<JobMatchConsultancy> jobMatchConsultancyList = new ArrayList<>();

        for (Map.Entry<Long, Set<Long>> entry : consultancyMatchedConsultancyUserMap.entrySet()) {
            Long consultancyId = entry.getKey();
            // insert all consultancies as applicants
            for (Long consultancyUserId : entry.getValue()) {
                JobMatch jobMatchObj = new JobMatch();
                jobMatchObj.setJobPostId(jobPost.getJobPostId());
                jobMatchObj.setRecruiterId(jobPost.getUserId());
                jobMatchObj.setApplicantId(consultancyId);
                jobMatchObj.setConsultancy(true);
                jobMatchObj.setCreatedDate(new Date());
                //  jobMatchObj.setUpdatedDate(new Date());
                jobMatchObj.setConsultancyUserId(consultancyUserId);
                jobMatchData.add(jobMatchObj);
                // Save consultancy users matched for job
                for (Long consultancyUserMatchedApplicant : consultancyUserMatchedConsultancyApplicantMap.get(consultancyUserId)) {
                    JobMatchConsultancy jobMatchForCons = new JobMatchConsultancy();
                    jobMatchForCons.setApplicantId(consultancyUserMatchedApplicant);
                    jobMatchForCons.setJobPostId(jobPost.getJobPostId());
                    jobMatchForCons.setConsultancyId(consultancyId);
                    jobMatchForCons.setRecruiterId(jobPost.getUserId());
                    jobMatchForCons.setConsultancyUserId(consultancyUserId);
                    jobMatchForCons.setCreatedDate(new Date());
                    jobMatchForCons.setUpdatedDate(new Date());
                    jobMatchForCons.setMatchScore(calculateMatchScore(jobPost, applicantMap.get(consultancyUserMatchedApplicant)
                            , applicantDetailsMap.get(consultancyUserMatchedApplicant), applicantJobFunctionMap.get(consultancyUserMatchedApplicant),
                            applicantJobSkillMap.get(consultancyUserMatchedApplicant), applicantSecondaryJobFunctionMap.get(consultancyUserMatchedApplicant), applicantDegreeMap.get(consultancyUserMatchedApplicant)));
                    int score = calculateScoreForRelevantApp(jobPost, applicantMap.get(consultancyUserMatchedApplicant)
                            , applicantDetailsMap.get(consultancyUserMatchedApplicant), applicantJobFunctionMap.get(consultancyUserMatchedApplicant),
                            applicantJobSkillMap.get(consultancyUserMatchedApplicant), applicantDegreeMap.get(consultancyUserMatchedApplicant));
                    if (score != 0) {
                        jobMatchForCons.setRecommended(RecommendedState.RELEVANT.getValue());
                        jobMatchForCons.setRecommendedScore(score);
                    } else {
                        score = calculateScoreForSuggestedApp(jobPost, applicantMap.get(consultancyUserMatchedApplicant)
                                , applicantDetailsMap.get(consultancyUserMatchedApplicant), applicantJobFunctionMap.get(consultancyUserMatchedApplicant),
                                applicantJobSkillMap.get(consultancyUserMatchedApplicant), applicantSecondaryJobFunctionMap.get(consultancyUserMatchedApplicant), applicantDegreeMap.get(consultancyUserMatchedApplicant));
                        jobMatchForCons.setRecommended(RecommendedState.SUGGESTED.getValue());
                        jobMatchForCons.setRecommendedScore(score);
                    }
                    jobMatchConsultancyList.add(jobMatchForCons);
                }
            }
        }
        try {

            jobMatchingRepositoryImpl.bulkSaveAllJobMatches(jobMatchData);
            // 6. insert matched consultancy users into another match table
            jobMatchingRepositoryImpl.bulkSaveAllJobMatchConsultancy(jobMatchConsultancyList);
            updateJobMatchScore(jobPost);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateJobMatchScore(JobPost jobPost) {
        try {

            List<JobMatchConsultancy> jobMatchIntrestedConsultancies = jobMatchConsultancyRepository.findByJobPostIdWithAction(jobPost.getJobPostId());

            List<Long> applicantIds = new ArrayList<>();
            for (JobMatchConsultancy jobMatchConsultancy : jobMatchIntrestedConsultancies) {
                applicantIds.add(jobMatchConsultancy.getApplicantId());
            }
            Set<Long> allApplicantIds = new HashSet<>();
            allApplicantIds.addAll(applicantIds);
            List<Applicant> applicants = new ArrayList<>();
            List<JobMatchConsultancy> jobMatchConsultancyList = new ArrayList<>();

            Map<Long, Applicant> applicantMap = new HashMap<>();
            Map<Long, ApplicantDetails> applicantDetailsMap = new HashMap<>();
            Map<Long, Set<Integer>> applicantJobSkillMap = new HashMap<>();
            Map<Long, Set<Integer>> applicantJobFunctionMap = new HashMap<>();
            Map<Long, Set<Integer>> applicantSecondaryJobFunctionMap = new HashMap<>();
            Map<Long, Set<Integer>> applicantDegreeMap = new HashMap<>();
            if (allApplicantIds.size() > 0) {
                applicants = (List<Applicant>) applicantRepository.findAllById(allApplicantIds);
                applicantMap = applicants.stream()
                        .collect(Collectors.toMap(Applicant::getApplicantId, Function.identity()));
                //get all applicant Details
                applicantDetailsMap = applicantDetailsRepository
                        .findAllByApplicantIdIn(allApplicantIds)
                        .stream()
                        .collect(Collectors.toMap(ApplicantDetails::getApplicantId, Function.identity()));

                //get all applicant job functions
                applicantJobFunctionMap = applicantJobFunctionRepository
                        .findAllByApplicantIdIn(allApplicantIds)
                        .stream()
                        .collect(Collectors.groupingBy(
                                ApplicantJobFunction::getApplicantId,
                                Collectors.mapping(ApplicantJobFunction::getJobFunctionId, Collectors.toSet())
                        ));

                //get all applicant job skills
                applicantJobSkillMap = applicantJobSkillRepository
                        .findAllByApplicantIdIn(allApplicantIds)
                        .stream()
                        .collect(Collectors.groupingBy(
                                ApplicantJobSkill::getApplicantId,
                                Collectors.mapping(ApplicantJobSkill::getJobSkillId, Collectors.toSet())
                        ));

                applicantSecondaryJobFunctionMap = applicantSecondaryJobFunctionRepository
                        .findAllByApplicantIdIn(allApplicantIds)
                        .stream()
                        .collect(Collectors.groupingBy(
                                ApplicantSecondaryJobFunction::getApplicantId,
                                Collectors.mapping(ApplicantSecondaryJobFunction::getJobFunctionId, Collectors.toSet())
                        ));
                applicantDegreeMap = educationHistoryRepository.findAllByApplicantIdIn(allApplicantIds)
                        .stream()
                        .collect(Collectors.groupingBy(
                                EducationHistory::getApplicantId,
                                Collectors.mapping(
                                        educationHistory -> {
                                            Degrees degree = degreesRepository.findByShortTitle(educationHistory.getDegree());
                                            return (degree != null) ? degree.getDegreeId() : null;
                                        },
                                        Collectors.toSet()
                                )
                        ));

                for (JobMatchConsultancy jobMatchForCons : jobMatchIntrestedConsultancies) {
                    long consultancyUserMatchedApplicant = jobMatchForCons.getApplicantId();
                    jobMatchForCons.setMatchScore(calculateMatchScore(jobPost, applicantMap.get(consultancyUserMatchedApplicant)
                            , applicantDetailsMap.get(consultancyUserMatchedApplicant), applicantJobFunctionMap.get(consultancyUserMatchedApplicant),
                            applicantJobSkillMap.get(consultancyUserMatchedApplicant), applicantSecondaryJobFunctionMap.get(consultancyUserMatchedApplicant), applicantDegreeMap.get(consultancyUserMatchedApplicant)));
                    int score = calculateScoreForRelevantApp(jobPost, applicantMap.get(consultancyUserMatchedApplicant)
                            , applicantDetailsMap.get(consultancyUserMatchedApplicant), applicantJobFunctionMap.get(consultancyUserMatchedApplicant),
                            applicantJobSkillMap.get(consultancyUserMatchedApplicant), applicantDegreeMap.get(consultancyUserMatchedApplicant));
                    if (score != 0) {
                        jobMatchForCons.setRecommended(RecommendedState.RELEVANT.getValue());
                        jobMatchForCons.setRecommendedScore(score);
                    } else {
                        score = calculateScoreForSuggestedApp(jobPost, applicantMap.get(consultancyUserMatchedApplicant)
                                , applicantDetailsMap.get(consultancyUserMatchedApplicant), applicantJobFunctionMap.get(consultancyUserMatchedApplicant),
                                applicantJobSkillMap.get(consultancyUserMatchedApplicant), applicantSecondaryJobFunctionMap.get(consultancyUserMatchedApplicant), applicantDegreeMap.get(consultancyUserMatchedApplicant));
                        jobMatchForCons.setRecommended(RecommendedState.SUGGESTED.getValue());
                        jobMatchForCons.setRecommendedScore(score);
                    }
                    jobMatchConsultancyList.add(jobMatchForCons);
                }
                // 6. insert matched consultancy users into another match table
                jobMatchingRepositoryImpl.bulkSaveAllJobMatchConsultancy(jobMatchConsultancyList);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int calculateMatchScore(JobPost jobPost, Applicant applicant, ApplicantDetails applicantDetails,
                                   Set<Integer> applJobFuncIdList, Set<Integer> applJobSkillIdList, Set<Integer> applSecJobFunIdList, Set<Integer> applDegreeList) {
        //976
        int matchScore = 0;
        Set<Integer> skillList = jobPost.getJobSkills().stream().map(JobSkills::getSkillId)
                .collect(Collectors.toSet());
        long expMin = jobPost.getExperienceMin();
        long expMax = jobPost.getExperienceMax();
//		long salaryMin = jobPost.getSalaryMin();
//		long salaryMax = jobPost.getSalaryMax();
        String location = jobPost.getLocation();
        Set<Integer> jobFuncList = jobPost.getJobFunction().stream().map(JobFunction::getJobFunctionId)
                .collect(Collectors.toSet());
        Set<Integer> optJobFuncList = jobPost.getOptionalJobfunctions() != null ? jobPost.getOptionalJobfunctions()
                .stream().map(JobFunction::getJobFunctionId).collect(Collectors.toSet()) : null;

        Integer jobType = jobPost.getJobType();

        String jobWorkType = jobPost.getWorklocType();
        Integer citizenship = jobPost.getCitizenship();
        String noticePeriod = jobPost.getNoticePeriod() != null ? NoticePeriod.getValueOf(jobPost.getNoticePeriod()).toString() : null;


        Set<Degrees> degree = jobPost.getJobDegrees();

        Set<Integer> degreesList = degree != null
                ? degree.stream().map(Degrees::getDegreeId).collect(Collectors.toSet())
                : null;

        // Job Function
        if (equalLists(jobFuncList, applJobFuncIdList)) {
            matchScore += 18;
        } else if (equalLists(optJobFuncList, applJobFuncIdList)) {
            matchScore += 10;
        } else if (equalLists(optJobFuncList, applSecJobFunIdList) || equalLists(jobFuncList, applSecJobFunIdList)) {
            matchScore += 8;
        }


        // Experience
        if (Float.valueOf(applicantDetails.getYearsOfExperience()) >= expMin && Float.valueOf(applicantDetails.getYearsOfExperience()) <= expMax) {
            // exp // 15
            matchScore += 15;
        } else if (Float.valueOf(applicantDetails.getYearsOfExperience()) >= (expMin - 1) && (Float.valueOf(applicantDetails.getYearsOfExperience()) * 12) <= (expMin * 12 - 1)) {
            // score 5
            matchScore += 5;
        } else if ((Float.valueOf(applicantDetails.getYearsOfExperience()) * 12) >= (expMax * 12 + 1) && (Float.valueOf(applicantDetails.getYearsOfExperience())) <= (expMax + 1)) {
            // score 10
            matchScore += 10;
        }

        // skills
//        if (skillList.size() == applJobSkillIdList.size() && equalLists(skillList, applJobSkillIdList)) {
//            matchScore += 20;
//        }


        if (equalLists(skillList, applJobSkillIdList)) {
            matchScore += 20;
        } else {
            matchScore += calculateSkillScore(skillList, applJobSkillIdList);
        }

//        if (equalLists(skillList, applJobSkillIdList)) {
//            matchScore += 20;
//        } else {
//            matchScore += calculateSkillScore(skillList, applJobSkillIdList);
//        }

        // location
        if (location != null && location.equalsIgnoreCase(applicant.getLocation())) {
            matchScore += 8;
        } else if (applicantDetails.isWillingToRelocate() && applicantDetails.getRelocation() != null && location != null && location.equalsIgnoreCase(applicantDetails.getRelocation())) {
            matchScore += 6;
        } else {
            matchScore += 1;
        }

        System.out.println(JobType.getValueOf(applicantDetails.getJobType()));
        // jobtype
        if (jobType != null && jobType == JobType.getValueOf(applicantDetails.getJobType())) {
            matchScore += 10;
        } else {
            matchScore += 2;
        }

        // work mode
        if (jobWorkType != null && applicantDetails.getPreferredWorkMode() != null && jobWorkType.contains(applicantDetails.getPreferredWorkMode())) {
            matchScore += 10;
        } else {
            matchScore += 2;
        }

        // citizenship
        if (citizenship == null || citizenship == Citizenship.getValueOf(applicantDetails.getCitizenship())) {
            matchScore += 10;
        } else {
            matchScore += 2;
        }

        // notice period
        if (noticePeriod != null && applicantDetails.getNoticePeriod() != null && noticePeriod.equalsIgnoreCase(applicantDetails.getNoticePeriod())) {
            matchScore += 5;
        } else if (noticePeriod != null && applicantDetails.getNoticePeriod() != null && !noticePeriod.equalsIgnoreCase(applicantDetails.getNoticePeriod())) {
            matchScore += 1;
        }
        if (noticePeriod == null && applicantDetails.getNoticePeriod() != null) {
            matchScore += 5;
        }

        //degree
//        if (anyCommonElements(applDegreeList, degreesList)) {
//            matchScore += 4;
//        } else {
//            matchScore += 1;
//        }
        if (equalLists(applDegreeList, degreesList)) {
            matchScore += 4;
        } else if (anyCommonElements(applDegreeList, degreesList)) {
            matchScore += 4;
        } else if (degreesList != null && !degreesList.isEmpty() && applDegreeList == null) {
            matchScore += 1;
        } else if (degreesList == null && applDegreeList.isEmpty()) {
            matchScore += 4;
        } else if (degreesList == null && !applDegreeList.isEmpty()) {
            matchScore += 4;
        } else {
            matchScore += 1;
        }
//        if (degreesList != null && !degreesList.isEmpty() && applDegreeList == null) {
//            matchScore += 1;
//        }
//        if (degreesList == null && applDegreeList.isEmpty()) {
//            matchScore += 4;
//        }
        // TODO:salary

        return matchScore;

    }

    private int calculateSkillScore(Set<Integer> jobSkillList, Set<Integer> applJobSkillIdList) {
        int totalSkills = jobSkillList.size();
        boolean hasMatchedSkills = jobSkillList.removeAll(applJobSkillIdList);
        if (hasMatchedSkills) {
            int matchedSkills = totalSkills - jobSkillList.size();
            return (int) ((double) matchedSkills / totalSkills * 20);
        }
        return 0;
    }

    /**
     * Run job matcher to display initial matched jobs for applicant
     *
     * @param applicantId
     * @param pageSize
     * @throws Exception
     */
//    @Transactional
//    public void runJobMatcherForApplicant(Long applicantId, int pageSize) throws Exception {
//
//        try {
//            // 1. get jobs in batch order by jobpost id
//            int pageNo = 0;
//            Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("jobPostId").descending());
//            // status = active and
//            Optional<Applicant> applicantOpt = applicantRepository.findById(applicantId);
//            if (!applicantOpt.isPresent()) {
//                throw new Exception("Applicant not found");
//            }
//            Applicant applicant = applicantOpt.get();
////            List<JobMatchConsultancy> jobMatchConsultancies = jobMatchConsultancyRepository.findByUserIddWithNoAction(applicant.getApplicantId());
////            jobMatchConsultancyRepository.deleteAll(jobMatchConsultancies);
//
//            // 2. fetch skills, exp, etx relevant for matching
//            List<JobMatch> jobMatchData = new ArrayList<>();
//            // List<Integer> skillList =
//            // applicant.getApplicantSkills().stream().map(JobSkills::getSkillId)
//            // .collect(Collectors.toList());
//            // long expMin = applicant.getExperienceMin();
//            // long expMax = applicant.getExperienceMax();
//            // String location = applicant.getLocation();
//            // List<Integer> jobFuncList =
//            // applicant.getJobFunction().stream().map(JobFunction::getJobFunctionId)
//            // .collect(Collectors.toList());
//
//            List<Integer> skillList = new ArrayList<Integer>();
//
//            long expMin = 3;
//            long expMax = 6;
//            String location = "";
//            List<Integer> jobFuncList = null;
//            Integer jobType = 0;
//
//            // 3. get matching jobs for user
//            List<UserInfoResultSet> matchedProfiles = jobPostRepository.findJobsForAppilcantByCriteria(skillList,
//                    jobFuncList, expMin, expMax, location, jobType, pageable);
//
//            // 4. seperate users from consultancy to other
//            for (UserInfoResultSet userInfoResultSet : matchedProfiles) {
//                JobMatch jobMatchObj = new JobMatch();
//                jobMatchObj.setJobPostId(userInfoResultSet.getJobPostId());
//                jobMatchObj.setRecruiterId(userInfoResultSet.getRecruiterId());
//                jobMatchObj.setApplicantId(applicantId);
//                jobMatchObj.setConsultancy(applicant.getConsultancyId() == 0 ? false : true);
//                jobMatchData.add(jobMatchObj);
//            }
//            // jobMatchingRepository.bulkSaveAllJobMatches(jobMatchData);
//            jobMatchingRepositoryImpl.bulkSaveAllJobMatches(jobMatchData);
//        } catch (Exception e) {
//            throw new Exception(e.getMessage());
//        }
//
//    }
    @Transactional
    public void runJobMatcherForApplicant(Long applicantId, Long consultantUserId, int pageSize, boolean delete) throws Exception {
        try {
            // 1. get jobs in batch order by jobpost id
            int pageNo = 0;
//            Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("jobPostId").descending());
            // status = active and
            Optional<Applicant> applicantOpt = applicantRepository.findById(applicantId);
            if (!applicantOpt.isPresent()) {
                throw new Exception("Applicant not found");
            }
            Applicant applicant = applicantOpt.get();
            if (applicant.isCorrectionRequired()) {
                return;
            }
            if (delete) {
                List<JobMatchConsultancy> jobMatchConsultancies = jobMatchConsultancyRepository.findByUserIddWithNoAction(applicant.getApplicantId());
                jobMatchConsultancyRepository.deleteAll(jobMatchConsultancies);
            }


            List<ApplicantJobSkill> applicantJobSkills = applicantJobSkillRepository.findByApplicantId(applicant.getApplicantId());
            ApplicantDetails applicantDetails = applicantDetailsRepository.findByApplicantId(applicant.getApplicantId());
            List<Integer> applJobSkillIdList = applicantJobSkills.stream().map(ApplicantJobSkill::getJobSkillId).collect(Collectors.toList());
            List<ApplicantJobFunction> applicantJobFunctions = applicantJobFunctionRepository.findByApplicantId(applicant.getApplicantId());
            List<Integer> applJobFuncIdList = applicantJobFunctions.stream().map(ApplicantJobFunction::getJobFunctionId).collect(Collectors.toList());
            List<ApplicantSecondaryJobFunction> applicantSecondaryJobFunctions = applicantSecondaryJobFunctionRepository.findByApplicantId(applicant.getApplicantId());
            List<Integer> applSecJobFuncIdList = applicantSecondaryJobFunctions.stream().map(ApplicantSecondaryJobFunction::getJobFunctionId).collect(Collectors.toList());
            Set<Integer> applJobSkillIdSet = applicantJobSkills.stream().map(ApplicantJobSkill::getJobSkillId).collect(Collectors.toSet());
            Set<Integer> applJobFuncIdSet = applicantJobFunctions.stream().map(ApplicantJobFunction::getJobFunctionId).collect(Collectors.toSet());
            Set<Integer> applSecJobFunIdSet = applicantSecondaryJobFunctions.stream().map(ApplicantSecondaryJobFunction::getJobFunctionId).collect(Collectors.toSet());
            List<EducationHistory> educationHistories = educationHistoryRepository.findByApplicantId(applicant.getApplicantId());
            Set<Integer> appDegreeIdSet = educationHistories.stream()
                    .map(EducationHistory::getDegree)
                    .map(degreesRepository::findByShortTitle)
                    .filter(Objects::nonNull)
                    .map(Degrees::getDegreeId)
                    .collect(Collectors.toSet());
//            long expMin = Long.valueOf(applicantDetails.getYearsOfExperience()) - 1;
//            long expMax = Long.valueOf(applicantDetails.getYearsOfExperience()) + 1;

//            long expMin = (long) (Float.valueOf(applicantDetails.getYearsOfExperience()) - 1);
//            long expMax = (long) (Float.valueOf(applicantDetails.getYearsOfExperience()) + 1);
//            List<Long> matchedJobPost = jobPostRepository.findJobsByCriteria(applJobSkillIdList, applJobFuncIdList, expMin, expMax, consultantUserId, applSecJobFuncIdList);
            List<Long> matchedJobPost = jobPostRepository.findJobsByCriteria(applJobSkillIdList, applJobFuncIdList, Float.valueOf(applicantDetails.getYearsOfExperience()), consultantUserId, applSecJobFuncIdList);

            List<JobPost> jobPosts = jobPostRepository.findAllById(matchedJobPost);
            List<JobMatch> jobMatchData = new ArrayList<>();
            List<JobMatchConsultancy> jobMatchConsultancyList = new ArrayList<>();
            for (JobPost jobPost : jobPosts) {
                JobMatch jobMatchObj = new JobMatch();
                jobMatchObj.setJobPostId(jobPost.getJobPostId());
                jobMatchObj.setRecruiterId(jobPost.getUserId());
                jobMatchObj.setApplicantId(applicant.getConsultancyId());
                jobMatchObj.setConsultancy(true);
                jobMatchObj.setCreatedDate(new Date());
                // jobMatchObj.setUpdatedDate(new Date());
                jobMatchObj.setConsultancyUserId(applicant.getConsultancyUserId());
                jobMatchData.add(jobMatchObj);
                // Save consultancy users matched for job
                JobMatchConsultancy jobMatchForCons = new JobMatchConsultancy();
                jobMatchForCons.setApplicantId(applicantId);
                jobMatchForCons.setJobPostId(jobPost.getJobPostId());
                jobMatchForCons.setConsultancyId(applicant.getConsultancyId());
                jobMatchForCons.setRecruiterId(jobPost.getUserId());
                jobMatchForCons.setConsultancyUserId(applicant.getConsultancyUserId());
                jobMatchForCons.setCreatedDate(new Date());
                jobMatchForCons.setUpdatedDate(new Date());
                jobMatchForCons.setMatchScore(calculateMatchScore(jobPost, applicant
                        , applicantDetails, applJobFuncIdSet,
                        applJobSkillIdSet, applSecJobFunIdSet, appDegreeIdSet));
                int score = calculateScoreForRelevantApp(jobPost, applicant
                        , applicantDetails, applJobFuncIdSet,
                        applJobSkillIdSet, appDegreeIdSet);
                if (score != 0) {
                    jobMatchForCons.setRecommended(RecommendedState.RELEVANT.getValue());
                    jobMatchForCons.setRecommendedScore(score);
                } else {
                    score = calculateScoreForSuggestedApp(jobPost, applicant
                            , applicantDetails, applJobFuncIdSet,
                            applJobSkillIdSet, applSecJobFunIdSet, appDegreeIdSet);
                    jobMatchForCons.setRecommended(RecommendedState.SUGGESTED.getValue());
                    jobMatchForCons.setRecommendedScore(score);
                }
                jobMatchConsultancyList.add(jobMatchForCons);
            }
            try {
                List<JobMatchConsultancy> jobMatchIntrestedConsultancies = jobMatchConsultancyRepository.findByUserIddWithAction(applicant.getApplicantId());
                for (JobMatchConsultancy jobMatchConsultancy : jobMatchIntrestedConsultancies) {
                    JobPost jobPost = jobPostRepository.findById(jobMatchConsultancy.getJobPostId()).get();
                    jobMatchConsultancy.setMatchScore(calculateMatchScore(jobPost, applicant
                            , applicantDetails, applJobFuncIdSet,
                            applJobSkillIdSet, applSecJobFunIdSet, appDegreeIdSet));
                    int score = calculateScoreForRelevantApp(jobPost, applicant
                            , applicantDetails, applJobFuncIdSet,
                            applJobSkillIdSet, appDegreeIdSet);
                    if (score != 0) {
                        jobMatchConsultancy.setRecommended(RecommendedState.RELEVANT.getValue());
                        jobMatchConsultancy.setRecommendedScore(score);
                    } else {
                        score = calculateScoreForSuggestedApp(jobPost, applicant
                                , applicantDetails, applJobFuncIdSet,
                                applJobSkillIdSet, applSecJobFunIdSet, appDegreeIdSet);
                        jobMatchConsultancy.setRecommended(RecommendedState.SUGGESTED.getValue());
                        jobMatchConsultancy.setRecommendedScore(score);
                    }
                    jobMatchConsultancyList.add(jobMatchConsultancy);
                }

                jobMatchingRepositoryImpl.bulkSaveAllJobMatches(jobMatchData);
                // 6. insert matched consultancy users into another match table
                jobMatchingRepositoryImpl.bulkSaveAllJobMatchConsultancy(jobMatchConsultancyList);

            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    public long changeJobStatus(long jobId, long consultantId, String jobStatus) throws WorkruitException, InterruptedException {
        JobPost jobPost = jobPostRepository.findById(jobId)
                .orElseThrow(() -> new WorkruitException(String.format("JobPost is not found with id: %s", jobId)));
        if (jobStatus.equals(JobStatus.ACTIVE.toString())) {
            jobPost.setActivatedDate(new Date());
            jobPost.setStatus(JobStatus.ACTIVE);
            jobPost.setUpdatedDate(new Date());
            jobPostRepository.save(jobPost);
            // Add saved job to queue to process the job matcher algo
            runJobMatcherForJob(jobId, consultantId);
        }
        if (jobStatus.equals(JobStatus.CLOSED.toString())) {
            jobPost.setClosedDate(new Date());
            jobPost.setStatus(JobStatus.CLOSED);
            jobPost.setUpdatedDate(new Date());
            jobPostRepository.save(jobPost);
        }

        return jobId;
    }

    public RecomProfilesViewResponse getProfileMatchedApplicants(long jobId, int recommended, Integer pageNo, Integer pageSize) throws WorkruitException {
        JobPost jobPost = jobPostRepository.findById(jobId).orElseThrow(() -> new WorkruitException(
                String.format("Job post is not found with id: %s", jobId)));
        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("recommendedScore").descending());
        Page<JobMatchConsultancy> matchedProfiles = jobMatchConsultancyRepository.findRecommendedProfiles(jobId, recommended, pageable);
        return matchedProfiles != null ? createProfilesDTO(matchedProfiles) : null;
    }

    public RecomProfilesViewResponse createProfilesDTO(Page<JobMatchConsultancy> matchedProfiles) {
        Set<Long> applicantIds = matchedProfiles.stream().map(JobMatchConsultancy::getApplicantId).collect(Collectors.toSet());
        Map<Long, Integer> applicantJobStatus = matchedProfiles.stream().collect(Collectors.toMap(JobMatchConsultancy::getApplicantId, JobMatchConsultancy::getApplicantJobStatus));
        Map<Long, Integer> profileMatchScore = matchedProfiles.stream().collect(Collectors.toMap(JobMatchConsultancy::getApplicantId, JobMatchConsultancy::getRecommendedScore));
        List<Applicant> applicants = new ArrayList<>();
        Map<Long, ApplicantDetails> applicantDetailsMap = new HashMap<>();
        if (applicantIds.size() > 0) {
            applicants = (List<Applicant>) applicantRepository.findAllById(applicantIds);
            //get all applicant Details
            applicantDetailsMap = applicantDetailsRepository
                    .findAllByApplicantIdIn(applicantIds)
                    .stream()
                    .collect(Collectors.toMap(ApplicantDetails::getApplicantId, Function.identity()));
        }
        RecomProfilesViewResponse recomProfilesViewResponse = new RecomProfilesViewResponse();
        List<RecommendedProfilesDTO> recommendedProfilesDTOS = new ArrayList<>();
        for (Applicant applicant : applicants) {
            RecommendedProfilesDTO recommendedProfilesDTO = new RecommendedProfilesDTO();
            AppledProfilesDTO appledProfilesDTO = new AppledProfilesDTO();
            appledProfilesDTO.setApplicantId(applicant.getApplicantId());
            appledProfilesDTO.setApplicantName(applicant.getFirstName() + " " + applicant.getLastName());
            WorkExperience workExperiences = workExperienceRepository
                    .findByWorkExperienceIdAndApplicantId(applicant.getApplicantId());
            appledProfilesDTO.setApplicantTitle(workExperiences.getJobTitle() + " : " + workExperiences.getCompanyName());
            appledProfilesDTO.setExperience(String.valueOf(applicantDetailsMap.get(applicant.getApplicantId()).getYearsOfExperience()));
            appledProfilesDTO.setJobFunc(applicantDetailsMap.get(applicant.getApplicantId()).getJobFunction());
            appledProfilesDTO.setSecondaryJobFunc(applicantDetailsMap.get(applicant.getApplicantId()).getSecondaryJobFunction());
            appledProfilesDTO.setLocation(applicant.getLocation());
            appledProfilesDTO.setApplicantId(applicant.getApplicantId());
            appledProfilesDTO.setApplicantStatus(applicantJobStatus.get(applicant.getApplicantId()));
            recommendedProfilesDTO.setApplicantProfilesDTO(appledProfilesDTO);
            recommendedProfilesDTO.setScore(profileMatchScore.get(applicant.getApplicantId()));
            recommendedProfilesDTOS.add(recommendedProfilesDTO);
        }
        recomProfilesViewResponse.setRecommendedProfilesDTO(recommendedProfilesDTOS.stream().sorted(Comparator.comparingInt(RecommendedProfilesDTO::getScore).reversed()).collect(Collectors.toList()));
        recomProfilesViewResponse.setTotalCount(matchedProfiles.getTotalElements());
        recomProfilesViewResponse.setTotalPages(matchedProfiles.getTotalPages());
        return recomProfilesViewResponse;
    }

    public int calculateScoreForRelevantApp(JobPost jobPost, Applicant applicant, ApplicantDetails applicantDetails,
                                            Set<Integer> applJobFuncIdList, Set<Integer> applJobSkillIdList, Set<Integer> applDegreeIdList) {
        int matchScore = 0;
        Set<Integer> skillList = jobPost.getJobSkills().stream().map(JobSkills::getSkillId)
                .collect(Collectors.toSet());
        long expMin = jobPost.getExperienceMin();
        long expMax = jobPost.getExperienceMax();
        String location = jobPost.getLocation();
        Set<Integer> jobFuncList = jobPost.getJobFunction().stream().map(JobFunction::getJobFunctionId)
                .collect(Collectors.toSet());
        Integer jobType = jobPost.getJobType();
        String jobWorkType = jobPost.getWorklocType();
        Integer citizenship = jobPost.getCitizenship();
        String noticePeriod = jobPost.getNoticePeriod() != null ? NoticePeriod.getValueOf(jobPost.getNoticePeriod()).toString() : null;
        Set<Degrees> degree = jobPost.getJobDegrees();
        Set<Integer> degreesList = degree != null
                ? degree.stream().map(Degrees::getDegreeId).collect(Collectors.toSet())
                : null;
        // Job Function
        if (equalLists(jobFuncList, applJobFuncIdList)) {
            matchScore += 18;
        } else {
            return 0;
        }
        // Experience
        if (Float.valueOf(applicantDetails.getYearsOfExperience()) >= expMin && Float.valueOf(applicantDetails.getYearsOfExperience()) <= expMax) {
            // exp // 15
            matchScore += 15;
        } else {
            return 0;
        }
        // skills
        if (anyCommonElements(skillList, applJobSkillIdList)) {
            matchScore += calculateSkillScore(skillList, applJobSkillIdList);
        } else {
            return 0;
        }
        // location
        if (location != null && location.equalsIgnoreCase(applicant.getLocation())) {
            matchScore += 8;
        } else {
            return 0;
        }
        // jobtype
        if (jobType != null && jobType == JobType.getValueOf(applicantDetails.getJobType())) {
            matchScore += 10;
        } else {
            return 0;
        }
        // work mode
        if (jobWorkType != null && jobWorkType.contains(applicantDetails.getPreferredWorkMode())) {
            matchScore += 10;
        } else {
            return 0;
        }
        // citizenship
        if (citizenship != null && citizenship == Citizenship.getValueOf(applicantDetails.getCitizenship())) {
            matchScore += 10;
        } else {
            return 0;
        }
        if (citizenship != null && citizenship == Citizenship.ALL.getValue()) {
            matchScore += 10;
        }
        // notice period
        if (noticePeriod != null && noticePeriod.equalsIgnoreCase(applicantDetails.getNoticePeriod())) {
            matchScore += 5;
        } else {
            return 0;
        }
        //degree
        if (equalLists(applDegreeIdList, degreesList)) {
            matchScore += 4;
        } else {
            return 0;
        }
        return matchScore;
    }

    public int calculateScoreForSuggestedApp(JobPost jobPost, Applicant applicant, ApplicantDetails applicantDetails,
                                             Set<Integer> applJobFuncIdList, Set<Integer> applJobSkillIdList, Set<Integer> applSecJobFunIdList, Set<Integer> applDegreeIdList) {
        int matchScore = 0;
        Set<Integer> skillList = jobPost.getJobSkills().stream().map(JobSkills::getSkillId)
                .collect(Collectors.toSet());
        long expMin = jobPost.getExperienceMin();
        long expMax = jobPost.getExperienceMax();
        String location = jobPost.getLocation();
        Set<Integer> optJobFuncList = jobPost.getOptionalJobfunctions() != null ? jobPost.getOptionalJobfunctions()
                .stream().map(JobFunction::getJobFunctionId).collect(Collectors.toSet()) : null;
        Integer jobType = jobPost.getJobType();
        String jobWorkType = jobPost.getWorklocType();
        Integer citizenship = jobPost.getCitizenship();
        String noticePeriod = jobPost.getNoticePeriod() != null ? NoticePeriod.getValueOf(jobPost.getNoticePeriod()).toString() : null;
        Set<Degrees> degree = jobPost.getJobDegrees();
        Set<Integer> degreesList = degree != null
                ? degree.stream().map(Degrees::getDegreeId).collect(Collectors.toSet())
                : null;
        int primaryAttributes = 0;
        // Job Function
        if (anyCommonElements(optJobFuncList, applJobFuncIdList)) {
            matchScore += 10;
            primaryAttributes++;
        }
        if (anyCommonElements(optJobFuncList, applSecJobFunIdList)) {
            matchScore += 8;
            primaryAttributes++;
        }
        // Experience
        if (Float.valueOf(applicantDetails.getYearsOfExperience()) >= (expMin - 1) && (Float.valueOf(applicantDetails.getYearsOfExperience()) * 12) <= (expMin * 12 - 1)) {
            matchScore += 5;
            primaryAttributes++;
        } else if ((Float.valueOf(applicantDetails.getYearsOfExperience()) * 12) >= (expMax * 12 + 1) && (Float.valueOf(applicantDetails.getYearsOfExperience())) <= (expMax + 1)) {
            matchScore += 10;
            primaryAttributes++;
        }
        // skills
        if (anyCommonElements(skillList, applJobSkillIdList)) {
            matchScore += calculateSkillScore(skillList, applJobSkillIdList);
            primaryAttributes++;
        }
        if (primaryAttributes >= 3) {
            // location
            if (location != null) {
                if (location.equalsIgnoreCase(applicant.getLocation())) {
                    matchScore += 8;
                } else if (location.equalsIgnoreCase(applicantDetails.getRelocation())) {
                    matchScore += 6;
                } else {
                    matchScore += 1;
                }
            }
            // jobtype
            if (jobType != null && jobType == JobType.getValueOf(applicantDetails.getJobType())) {
                matchScore += 10;
            } else {
                matchScore += 2;
            }
            // work mode
            if (jobWorkType != null && jobWorkType.contains(applicantDetails.getPreferredWorkMode())) {
                matchScore += 10;
            } else {
                matchScore += 2;
            }
            // citizenship
            if (citizenship != null && citizenship == Citizenship.getValueOf(applicantDetails.getCitizenship())) {
                matchScore += 10;
            } else {
                matchScore += 2;
            }
            if (citizenship != null && citizenship == Citizenship.ALL.getValue()) {
                matchScore += 10;
            }
            // notice period
            if (noticePeriod != null && noticePeriod.equalsIgnoreCase(applicantDetails.getNoticePeriod())) {
                matchScore += 5;
            } else {
                matchScore += 1;
            }
            //degree comparison
            if (equalLists(applDegreeIdList, degreesList)) {
                matchScore += 4;
            } else {
                if (!equalLists(applDegreeIdList, degreesList)) {
                    matchScore += 1;
                }
            }
            if (degreesList != null && !degreesList.isEmpty() && applDegreeIdList == null) {
                matchScore += 1;
            }
            if ((degreesList == null || degreesList.isEmpty()) && (applDegreeIdList != null && !applDegreeIdList.isEmpty())) {
                matchScore += 4;
            }
        }
        return matchScore;
    }

    public int calculateAppFilterScore(long applicantId, MatchedJobsFilter matchedJobsFilter, TalentFilterDTO talentFilterDTO) throws Exception {
        Applicant applicant = applicantRepository.findById(applicantId).orElseThrow(() -> new Exception("Applicant not found"));

        List<ApplicantJobSkill> applicantJobSkills = applicantJobSkillRepository.findByApplicantId(applicant.getApplicantId());
        ApplicantDetails applicantDetails = applicantDetailsRepository.findByApplicantId(applicant.getApplicantId());
        List<ApplicantJobFunction> applicantJobFunctions = applicantJobFunctionRepository.findByApplicantId(applicant.getApplicantId());
        List<Integer> applJobFuncIdList = applicantJobFunctions.stream().map(ApplicantJobFunction::getJobFunctionId).collect(Collectors.toList());
        List<ApplicantSecondaryJobFunction> applicantSecondaryJobFunctions = applicantSecondaryJobFunctionRepository.findByApplicantId(applicant.getApplicantId());
        Set<Integer> applJobSkillIdSet = applicantJobSkills.stream().map(ApplicantJobSkill::getJobSkillId).collect(Collectors.toSet());
        List<Integer> applSecJobFunIdList = applicantSecondaryJobFunctions.stream().map(ApplicantSecondaryJobFunction::getJobFunctionId).collect(Collectors.toList());
        List<EducationHistory> educationHistories = educationHistoryRepository.findByApplicantId(applicant.getApplicantId());
        Set<Integer> appDegreeIdSet = educationHistories.stream()
                .map(EducationHistory::getDegree)
                .map(degreesRepository::findByShortTitle)
                .filter(Objects::nonNull)
                .map(Degrees::getDegreeId)
                .collect(Collectors.toSet());
        int matchScore = 0;

        //MatchedJobsFilter is considered as JobPost for calculate matchScore from consultancy side
        if (matchedJobsFilter != null) {
            if (matchedJobsFilter.getJobFunction() != null && matchedJobsFilter.getJobFunction() != 0) {
                if (applJobFuncIdList.contains(matchedJobsFilter.getJobFunction())) {
                    matchScore += 18;
                } else if (applSecJobFunIdList.contains(matchedJobsFilter.getJobFunction())) {
                    matchScore += 8;
                }
            }

            if (matchedJobsFilter.getLocations() != null && !matchedJobsFilter.getLocations().isEmpty()) {
                if (matchedJobsFilter.getLocations().stream().anyMatch(e -> e.equalsIgnoreCase(applicant.getLocation()))) {
                    matchScore += 8;
                } else if (applicantDetails.isWillingToRelocate() && applicantDetails.getRelocation() != null && matchedJobsFilter.getLocations().stream().anyMatch(e -> e.equalsIgnoreCase(applicantDetails.getRelocation()))) {
                    matchScore += 6;
                }
            } else {
                matchScore += 1;
            }

            if (matchedJobsFilter.getJobTypes() != null && !matchedJobsFilter.getJobTypes().isEmpty() && matchedJobsFilter.getJobTypes().stream().anyMatch(e -> e == JobType.getValueOf(applicantDetails.getJobType()))) {
                matchScore += 10;
            } else {
                matchScore += 2;
            }

            if (matchedJobsFilter.getWorklocValue() != null && !matchedJobsFilter.getWorklocValue().isEmpty() && matchedJobsFilter.getWorklocValue().stream().anyMatch(e -> e.equalsIgnoreCase(applicantDetails.getPreferredWorkMode()))) {
                matchScore += 10;
            } else {
                matchScore += 2;
            }

            if (matchedJobsFilter.getCitizenship() != null && !matchedJobsFilter.getCitizenship().isEmpty() && matchedJobsFilter.getCitizenship().stream().anyMatch(e -> e == Citizenship.getValueOf(applicantDetails.getCitizenship()))) {
                matchScore += 10;
            } else {
                matchScore += 2;
            }

            if (matchedJobsFilter.getEduQualification() != null && !matchedJobsFilter.getEduQualification().isEmpty()) {
                if (equalLists(appDegreeIdSet, matchedJobsFilter.getEduQualification().stream().collect(Collectors.toSet()))) {
                    matchScore += 4;
                } else {
                    matchScore += 1;
                }
                if (educationHistories == null && matchedJobsFilter.getEduQualification() != null) {
                    matchScore += 1;
                }
            }
            if (matchedJobsFilter.getEduQualification() == null && educationHistories != null) {
                matchScore += 4;
            } else if (matchedJobsFilter.getEduQualification() == null && educationHistories == null) {
                matchScore += 4;
            }

            if (matchedJobsFilter.getJobSkills() != null && !matchedJobsFilter.getJobSkills().isEmpty()) {
                Set<Integer> skillSet = matchedJobsFilter.getJobSkills().stream().collect(Collectors.toSet());
                if (equalLists(skillSet, applJobSkillIdSet)) {
                    matchScore += 20;
                } else {
                    matchScore += calculateSkillScore(skillSet, applJobSkillIdSet);
                }
            }

            if (matchedJobsFilter.getNoticePeriod() == null || matchedJobsFilter.getNoticePeriod().isEmpty() || applicantDetails.getNoticePeriod() != null || matchedJobsFilter.getNoticePeriod().stream().anyMatch(e -> e == NoticePeriod.getValueOf(applicantDetails.getNoticePeriod()))) {
                matchScore += 5;
            } else {
                matchScore += 1;
            }
        }

        //TalentFilterDTO is considered as JobPost for calculate matchScore  from company side
        if (talentFilterDTO != null) {
            if (talentFilterDTO.getJobFunction() != null && talentFilterDTO.getJobFunction() != 0) {
                if (applJobFuncIdList.contains(talentFilterDTO.getJobFunction())) {
                    matchScore += 18;
                } else if (applSecJobFunIdList.contains(talentFilterDTO.getJobFunction())) {
                    matchScore += 8;
                }
            }

            if (talentFilterDTO.getLocation() != null && !talentFilterDTO.getLocation().isEmpty()) {
                if (talentFilterDTO.getLocation().equalsIgnoreCase(applicant.getLocation())) {
                    matchScore += 8;
                } else if (applicantDetails.isWillingToRelocate() && applicantDetails.getRelocation() != null && talentFilterDTO.getLocation().equalsIgnoreCase(applicantDetails.getRelocation())) {
                    matchScore += 6;
                }
            } else {
                matchScore += 1;
            }

            if (talentFilterDTO.getJobTypes() != null && !talentFilterDTO.getJobTypes().isEmpty() && talentFilterDTO.getJobTypes().stream().anyMatch(e -> e.equalsIgnoreCase(applicantDetails.getJobType()))) {
                matchScore += 10;
            } else {
                matchScore += 2;
            }

            if (talentFilterDTO.getWorkMode() != null && !talentFilterDTO.getWorkMode().isEmpty() && talentFilterDTO.getWorkMode().stream().anyMatch(e -> e.equalsIgnoreCase(applicantDetails.getPreferredWorkMode()))) {
                matchScore += 10;
            } else {
                matchScore += 2;
            }

            if (talentFilterDTO.getCitizenship() != null && !talentFilterDTO.getCitizenship().isEmpty() && talentFilterDTO.getCitizenship().stream().anyMatch(e -> e.equalsIgnoreCase(applicantDetails.getCitizenship()))) {
                matchScore += 10;
            } else {
                matchScore += 2;
            }

            if (talentFilterDTO.getEduQualification() != null && !talentFilterDTO.getEduQualification().isEmpty()) {
//                Set<Integer> talentDegreeIdSet = talentFilterDTO.getEduQualification().stream()
//                        .map(degreesRepository::)
//                        .filter(Objects::nonNull)
//                        .map(Degrees::getDegreeId)
//                        .collect(Collectors.toSet());
                if (equalLists(appDegreeIdSet, talentFilterDTO.getEduQualification().stream().collect(Collectors.toSet()))) {
                    matchScore += 4;
                } else {
                    matchScore += 1;
                }
                if (educationHistories == null) {
                    matchScore += 1;
                }
            }
            if (talentFilterDTO.getEduQualification() == null && educationHistories.isEmpty()) {
                matchScore += 4;
            }

            if (talentFilterDTO.getJobSkills() != null && !talentFilterDTO.getJobSkills().isEmpty()) {
                Set<Integer> skillSet = talentFilterDTO.getJobSkills().stream().collect(Collectors.toSet());
                if (equalLists(skillSet, applJobSkillIdSet)) {
                    matchScore += 20;
                } else {
                    matchScore += calculateSkillScore(skillSet, applJobSkillIdSet);
                }
            }

            if (talentFilterDTO.getNoticePeriod() == null || talentFilterDTO.getNoticePeriod().isEmpty() || applicantDetails.getNoticePeriod() != null || talentFilterDTO.getNoticePeriod().stream().anyMatch(e -> e.equalsIgnoreCase(applicantDetails.getNoticePeriod()))) {
                matchScore += 5;
            } else {
                matchScore += 1;
            }

            if (talentFilterDTO.getExpMin() != null && talentFilterDTO.getExpMax() != null) {
                if (Float.valueOf(applicantDetails.getYearsOfExperience()) >= talentFilterDTO.getExpMin() && Float.valueOf(applicantDetails.getYearsOfExperience()) <= talentFilterDTO.getExpMax()) {
                    // exp // 15
                    matchScore += 15;
                } else if (Float.valueOf(applicantDetails.getYearsOfExperience()) >= (talentFilterDTO.getExpMin() - 1) && (Float.valueOf(applicantDetails.getYearsOfExperience()) * 12) <= (talentFilterDTO.getExpMin() * 12 - 1)) {
                    // score 5
                    matchScore += 5;
                } else if ((Float.valueOf(applicantDetails.getYearsOfExperience()) * 12) >= (talentFilterDTO.getExpMax() * 12 + 1) && (Float.valueOf(applicantDetails.getYearsOfExperience())) <= (talentFilterDTO.getExpMax() + 1)) {
                    // score 10
                    matchScore += 10;
                }
            }
        }

        return matchScore;
    }

}
