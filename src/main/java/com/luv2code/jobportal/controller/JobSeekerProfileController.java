package com.luv2code.jobportal.controller;

import com.luv2code.jobportal.entitiy.JobSeekerProfile;
import com.luv2code.jobportal.entitiy.Skills;
import com.luv2code.jobportal.entitiy.Users;
import com.luv2code.jobportal.repository.UsersRepository;
import com.luv2code.jobportal.services.JobSeekerProfileService;
import com.luv2code.jobportal.util.FileDownloadUtil;
import com.luv2code.jobportal.util.FileUploadUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Controller
@RequestMapping("/job-seeker-profile")
public class JobSeekerProfileController {

    private JobSeekerProfileService jobSeekerProfileService;
    private UsersRepository usersRepository;

    @Autowired
    public JobSeekerProfileController(JobSeekerProfileService jobSeekerProfileService, UsersRepository usersRepository) {
        this.jobSeekerProfileService = jobSeekerProfileService;
        this.usersRepository = usersRepository;
    }

    /*
    method to show job-seeker profile
    1. create an empty job-seeker profile
    2. get some of the security info
    3. adding this info the model accordingly
     */
    @GetMapping("/")
    public String JobSeekerProfile(Model model) {
        JobSeekerProfile  jobSeekerProfile = new JobSeekerProfile();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        List<Skills> skills = new ArrayList<>();

        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            Users user = usersRepository.findByEmail(authentication.getName()).orElseThrow(
                    () -> new UsernameNotFoundException("User not found"));

            Optional<JobSeekerProfile> seekerProfile = jobSeekerProfileService.getOne(user.getUserId());

            if (seekerProfile.isPresent()) {
                jobSeekerProfile = seekerProfile.get();

                if (jobSeekerProfile.getSkills().isEmpty()) {
                    skills.add(new Skills());
                    jobSeekerProfile.setSkills(skills);
                }
            }
            model.addAttribute("skills", skills);
            model.addAttribute("profile", jobSeekerProfile);
        }

        return "job-seeker-profile";
    }

    // When this form submission happens, we have some data that's passed in. we will get the job-seeker profile.
    @PostMapping("/addNew")
    public String addNew(JobSeekerProfile jobSeekerProfile,
                         @RequestParam("image") MultipartFile image,
                         @RequestParam("pdf") MultipartFile pdf,
                         Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            Users user = usersRepository.findByEmail(authentication.getName()).orElseThrow(
                    () -> new UsernameNotFoundException("User not found"));
            jobSeekerProfile.setUserId(user);
            jobSeekerProfile.setUserAccountId(user.getUserId());
        }
        List<Skills> skillsList = new ArrayList<>();
        model.addAttribute("profile", jobSeekerProfile);
        model.addAttribute("skills", skillsList);

        // associate the skills with the appropriate "job-seeker-profile" accordingly
        for (Skills skills : jobSeekerProfile.getSkills()) {
            skills.setJobSeekerProfile(jobSeekerProfile);
        }

        // handling the file upload for the profile image and resume
        String imageName = "";
        String resumeName = "";

        if (Objects.equals(image.getOriginalFilename(), "")) {
            imageName = StringUtils.cleanPath(Objects.requireNonNull(image.getOriginalFilename()));
            jobSeekerProfile.setProfilePhoto(imageName);
        }

        if (Objects.equals(pdf.getOriginalFilename(), "")) {
            resumeName = StringUtils.cleanPath(Objects.requireNonNull(pdf.getOriginalFilename()));
            jobSeekerProfile.setResume(resumeName);
        }
        /*
        Now at this moment is only in memory. we need to save it to our database.
        We can do that by making use of our jobSeekerProfileService. we have a call to this method "addNew".
         */

        JobSeekerProfile seekerProfile = jobSeekerProfileService.addNew(jobSeekerProfile);

        /*
         save the file to the file system. we store them in our directory here "photos/candidate/" based on user account ID.
         we'll make sure of our "FileUploadUtil" to save the file for the profile image. (similar for resume)
         */

        String uploadDir = "photos/candidate/" + jobSeekerProfile.getUserAccountId();

        try {

            if(!Objects.equals(image.getOriginalFilename(), "")) {
                FileUploadUtil.saveFile(uploadDir, imageName, image);
            }

            if(!Objects.equals(pdf.getOriginalFilename(), "")) {
                FileUploadUtil.saveFile(uploadDir, resumeName, pdf);
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        return "redirect:/dashboard/";
    }

    /*
     method to add "a request mapping" to "job-seeker-profile/{id} in job-details.html
     - show the profile or retrieve the profile for a given candidate ID
     - just buying the ID and pass in the model
     */
    @GetMapping("/{id}")
    public String candidateProfile(@PathVariable("id") int id, Model model) {
        // use the JobSeekerProfileService to retrieve that profile
        Optional<JobSeekerProfile> seekerProfile = jobSeekerProfileService.getOne(id);
        // add it to the model as an attribute with the name of profile
        model.addAttribute("profile", seekerProfile.get());

        return "job-seeker-profile"; // return to file "job-seeker-profile.html"
    }

    // set up the get mapping download resume
    @GetMapping("/downloadResume")
    public ResponseEntity<?> downloadResume(@RequestParam(value = "fileName") String fileName,
                                            @RequestParam(value = "userID") String userId) {
        // pass in the request params for the file name and actual id

        // make sure of the "FileDownloadUtil" class is used. simply give the download directory and file name.
        FileDownloadUtil fileDownloadUtil = new FileDownloadUtil();
        Resource resource = null;

        try {
            resource = fileDownloadUtil.getFileAsResource(("photos/candidate/") + userId, fileName);
        } catch (IOException io) {
            return  ResponseEntity.badRequest().build();
        }

        // if the resource is null, that means we did not find it
        if (resource == null) {
            return new ResponseEntity<>("File not found", HttpStatus.NOT_FOUND);
        }

        /*
        set up the content type and the actual header value, because we're going to send back a file to download
        - so we set that accordingly with the content type of application octet stream
        - that way our browser will know that we're sending over a binary file or a stream of binary or octets.
        - then we give the name of that file that we're passing over as far as that download.
         */
        String contentType = "application/octet-stream";
        String headerValue = "attachment; filename=\"" + resource.getFilename() + "\"";

        // return a response with content type headers and then actual resource.
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType))
                /*
                that resource is the actual contents of the file, and that will be in the response body.
                - the browser will get that whole stream of binary data and use it accordingly, or save it to your local computer
                 */
                .header(HttpHeaders.CONTENT_DISPOSITION, headerValue)
                .body(resource);
    }
}
