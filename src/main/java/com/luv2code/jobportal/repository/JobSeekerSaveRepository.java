package com.luv2code.jobportal.repository;

import com.luv2code.jobportal.entitiy.JobPostActivity;
import com.luv2code.jobportal.entitiy.JobSeekerProfile;
import com.luv2code.jobportal.entitiy.JobSeekerSave;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobSeekerSaveRepository extends JpaRepository<JobSeekerSave, Integer> {

    List<JobSeekerSave> findByUserId(JobSeekerProfile userAccountId);

    List<JobSeekerSave> findByJob(JobPostActivity job);

}
