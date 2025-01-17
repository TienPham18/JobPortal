package com.luv2code.jobportal.controller;

import com.luv2code.jobportal.entitiy.*;
import com.luv2code.jobportal.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Controller
public class JobSeekerApplyController {

    private final JobPostActivityService jobPostActivityService;
    private final UsersService usersService;

    private final JobSeekerApplyService jobSeekerApplyService;
    private final JobSeekerSaveService jobSeekerSaveService;

    private final RecruiterProfileService recruiterProfileService;
    private final JobSeekerProfileService jobSeekerProfileService;

    @Autowired
    public JobSeekerApplyController(JobPostActivityService jobPostActivityService, UsersService usersService, JobSeekerApplyService jobSeekerApplyService, JobSeekerSaveService jobSeekerSaveService, RecruiterProfileService recruiterProfileService, JobSeekerProfileService jobSeekerProfileService) {
        this.jobPostActivityService = jobPostActivityService;
        this.usersService = usersService;
        this.jobSeekerApplyService = jobSeekerApplyService;
        this.jobSeekerSaveService = jobSeekerSaveService;
        this.recruiterProfileService = recruiterProfileService;
        this.jobSeekerProfileService = jobSeekerProfileService;
    }

    // method for displaying the details for a given job.
    @GetMapping("/job-details-apply/{id}")
    public String display(@PathVariable("id") int id, Model model) {
        JobPostActivity jobDetails = jobPostActivityService.getOne(id);

        // get a list of job, candidates that have applied for a given job
        List<JobSeekerApply> jobSeekerApplyList = jobSeekerApplyService.getJobCandidates(jobDetails);
        List<JobSeekerSave> jobSeekerSaveList = jobSeekerSaveService.getJobCandidates(jobDetails);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("Recruiter"))) {
                RecruiterProfile user = recruiterProfileService.getCurrentRecruiterProfile();

                if (user != null) {
                    model.addAttribute("applyList", jobSeekerApplyList);
                }
            } else { // we know they are not recruiter, they must be a job-seeker so get that appropriate info for that user
                JobSeekerProfile user = jobSeekerProfileService.getCurrentSeekerProfile();

                // set up some flags accordingly. we loop through all the job-seeker applies and set those flags accordingly.
                if (user != null) {
                    boolean exist = false;
                    boolean saved = false;

                    for (JobSeekerApply jobSeekerApply : jobSeekerApplyList) {
                        if (jobSeekerApply.getUserId().getUserAccountId() == user.getUserAccountId()) {
                            exist = true;
                            break;
                        }
                    }

                    for (JobSeekerSave jobSeekerSave : jobSeekerSaveList) {
                        if (jobSeekerSave.getUserId().getUserAccountId() == user.getUserAccountId()) {
                            saved = true;
                            break;
                        }
                    }

                    // we simply add those flags to the actual model for already applied and also already saved.
                    model.addAttribute("alreadyApplied", exist);
                    model.addAttribute("alreadySaved", saved);
                }
            }
        }
        // create a new instance of this JobSeekerApply, basically our model attribute for the actual form data
        JobSeekerApply jobSeekerApply = new JobSeekerApply();
        model.addAttribute("applyJob", jobSeekerApply);

        // from beginning
        model.addAttribute("jobDetails", jobDetails);
        model.addAttribute("user", usersService.getCurrentUserProfile());
        return "job-details";
    }

    // method to persist the job when user applies
    @PostMapping("job-details/apply/{id}")
    public String apply(@PathVariable("id") int id, JobSeekerApply jobSeekerApply) {
        // Job-seekerApply actually contains the form data for then the user actually applied for that given job

        // normal work here of finding out the actual user that is currently logged in.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            String currentUsername = authentication.getName();
            Users user = usersService.findByEmail(currentUsername);

            // get the actual job-seeker profile and actual job post activity.
            Optional<JobSeekerProfile> seekerProfile = jobSeekerProfileService.getOne(user.getUserId());
            JobPostActivity jobPostActivity = jobPostActivityService.getOne(id);

            if (seekerProfile.isPresent() && jobPostActivity != null) {
                // set the values accordingly here
                jobSeekerApply = new JobSeekerApply();
                jobSeekerApply.setUserId(seekerProfile.get());
                jobSeekerApply.setJob((jobPostActivity));
                jobSeekerApply.setApplyDate(new Date());
            } else {
                throw new RuntimeException(("User not found"));
            }
            // adding a new item, our new job that this candidate is applying for
            jobSeekerApplyService.addNew(jobSeekerApply);
        }

        return "redirect:/dashboard/";
    }

}
