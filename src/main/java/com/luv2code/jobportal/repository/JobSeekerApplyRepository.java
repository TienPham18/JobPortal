package com.luv2code.jobportal.repository;

import com.luv2code.jobportal.entitiy.JobPostActivity;
import com.luv2code.jobportal.entitiy.JobSeekerApply;
import com.luv2code.jobportal.entitiy.JobSeekerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobSeekerApplyRepository extends JpaRepository<JobSeekerApply, Integer> {

    List<JobSeekerApply> findByUserId(JobSeekerProfile userId);

    List<JobSeekerApply> findByJob(JobPostActivity job);
}
