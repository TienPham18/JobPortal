package com.luv2code.jobportal.services;

import com.luv2code.jobportal.entitiy.JobSeekerProfile;
import com.luv2code.jobportal.entitiy.RecruiterProfile;
import com.luv2code.jobportal.entitiy.Users;
import com.luv2code.jobportal.repository.JobSeekerProfileRepository;
import com.luv2code.jobportal.repository.RecruiterProfileRepository;
import com.luv2code.jobportal.repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
public class UsersService {

    private final UsersRepository usersRepository;
    private final JobSeekerProfileRepository jobSeekerProfileRepository;
    private final RecruiterProfileRepository recruiterProfileRepository;

    private final PasswordEncoder passwordEncoder;

    // Constructor injection
    @Autowired
    public UsersService(UsersRepository theUsersRepository,
                        JobSeekerProfileRepository theJobSeekerProfileRepository,
                        RecruiterProfileRepository theRecruiterProfileRepository,
                        PasswordEncoder thePasswordEncoder) {
        this.usersRepository = theUsersRepository;
        this.jobSeekerProfileRepository = theJobSeekerProfileRepository;
        this.recruiterProfileRepository = theRecruiterProfileRepository;
        this.passwordEncoder = thePasswordEncoder;
    }

    public Users addNew(Users theUsers) {
        theUsers.setActive(true);
        theUsers.setRegistrationDate(new Date(System.currentTimeMillis()));
        theUsers.setPassword(passwordEncoder.encode(theUsers.getPassword()));

        // really important we need to save it first before we pass it into those other methods (refactoring from the below return)
        Users savedUser = usersRepository.save(theUsers);

        // retrieve the user type Id for this given user
        int userTypeId = theUsers.getUserTypeId().getUserTypeId();
        if (userTypeId == 1) { // means they are a recruiter
            recruiterProfileRepository.save(new RecruiterProfile(savedUser));
        } else  {
            jobSeekerProfileRepository.save(new JobSeekerProfile(savedUser));
        }

        return savedUser;
        //return usersRepository.save(theUsers);

    }

    public Optional<Users> getUserByEmail(String email) {
        return usersRepository.findByEmail(email);
    }

    public Object getCurrentUserProfile() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            String username = authentication.getName();
            Users users = usersRepository.findByEmail(username).orElseThrow(
                    () -> new UsernameNotFoundException("User not found"));

            int userId = users.getUserId();

            if(authentication.getAuthorities().contains(new SimpleGrantedAuthority("Recruiter"))){
                RecruiterProfile recruiterProfile = recruiterProfileRepository.findById(userId).orElse(new RecruiterProfile());
                return recruiterProfile;
            } else {
                JobSeekerProfile jobSeekerProfile = jobSeekerProfileRepository.findById(userId).orElse(new JobSeekerProfile());
                return jobSeekerProfile;
            }
        }
        return null;
    }
}
