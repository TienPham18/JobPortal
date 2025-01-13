package com.luv2code.jobportal.services;

import com.luv2code.jobportal.entitiy.JobSeekerProfile;
import com.luv2code.jobportal.entitiy.RecruiterProfile;
import com.luv2code.jobportal.entitiy.Users;
import com.luv2code.jobportal.repository.JobSeekerProfileRepository;
import com.luv2code.jobportal.repository.RecruiterProfileRepository;
import com.luv2code.jobportal.repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
public class UsersService {

    private final UsersRepository usersRepository;
    private final JobSeekerProfileRepository jobSeekerProfileRepository;
    private final RecruiterProfileRepository recruiterProfileRepository;

    // Constructor injection
    @Autowired
    public UsersService(UsersRepository theUsersRepository,
                        JobSeekerProfileRepository theJobSeekerProfileRepository,
                        RecruiterProfileRepository theRecruiterProfileRepository) {
        this.usersRepository = theUsersRepository;
        this.jobSeekerProfileRepository = theJobSeekerProfileRepository;
        this.recruiterProfileRepository = theRecruiterProfileRepository;
    }

    public Users addNew(Users theUsers) {
        theUsers.setActive(true);
        theUsers.setRegistrationDate(new Date(System.currentTimeMillis()));
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
}
