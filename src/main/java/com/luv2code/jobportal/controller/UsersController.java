package com.luv2code.jobportal.controller;

import com.luv2code.jobportal.entitiy.Users;
import com.luv2code.jobportal.entitiy.UsersType;
import com.luv2code.jobportal.services.UsersService;
import com.luv2code.jobportal.services.UsersTypeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;
import java.util.Optional;

@Controller
public class UsersController {

    private final UsersTypeService usersTypeService;
    private final UsersService usersService;

    // constructor injection
    @Autowired
    public UsersController(UsersTypeService theUsersTypeService, UsersService theUsersService) {
        this.usersTypeService = theUsersTypeService;
        this.usersService = theUsersService;
    }

    @GetMapping("/register")
    public String register(Model theModel) {
        List<UsersType> usersTypes = usersTypeService.getAll();
        theModel.addAttribute("getAllTypes", usersTypes);
        theModel.addAttribute("user", new Users());

        return "register";
    }

    @PostMapping("register/new")
    public String userRegistration(@Valid Users theUsers, Model theModel) {
        System.out.println("User: " + theUsers);

        Optional<Users> optionalUsers = usersService.getUserByEmail(theUsers.getEmail());

        if (optionalUsers.isPresent()) {
            theModel.addAttribute("error", "Email already registered");

            List<UsersType> usersTypes = usersTypeService.getAll();
            theModel.addAttribute("getAllTypes", usersTypes);
            theModel.addAttribute("user", new Users());

            return "register";

        }
        usersService.addNew(theUsers);
        return "redirect:/dashboard/";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null) {
            new SecurityContextLogoutHandler().logout(request, response, authentication);
        }
        return "redirect:/";
    }
}
