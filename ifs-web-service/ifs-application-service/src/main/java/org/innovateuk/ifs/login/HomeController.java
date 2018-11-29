package org.innovateuk.ifs.login;

import org.innovateuk.ifs.commons.error.ValidationMessages;
import org.innovateuk.ifs.commons.security.SecuredBySpring;
import org.innovateuk.ifs.controller.ValidationHandler;
import org.innovateuk.ifs.login.form.RoleSelectionForm;
import org.innovateuk.ifs.login.viewmodel.RoleSelectionViewModel;
import org.innovateuk.ifs.user.resource.Role;
import org.innovateuk.ifs.user.resource.UserResource;
import org.innovateuk.ifs.util.CookieUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.function.Supplier;

import static java.lang.String.format;
import static org.innovateuk.ifs.controller.ErrorToObjectErrorConverterFactory.asGlobalErrors;
import static org.innovateuk.ifs.controller.ErrorToObjectErrorConverterFactory.fieldErrorsToFieldErrors;
import static org.springframework.util.StringUtils.hasText;

/**
 * This Controller redirects the request from http://<domain>/ to http://<domain>/login
 * So we don't have a public homepage, the login page is the homepage.
 */
@Controller
@SecuredBySpring(value = "Controller", description = "TODO", securedType = HomeController.class)
@PreAuthorize("permitAll")
public class HomeController {

    @Autowired
    private CookieUtil cookieUtil;

    public static String getRedirectUrlForUser(UserResource user) {

        String roleUrl = !user.getRoles().isEmpty() ? user.getRoles().get(0).getUrl() : "";

        return format("redirect:/%s", hasText(roleUrl) ? roleUrl : "dashboard");
    }

    @GetMapping("/")
    public String login(Model model) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (unauthenticated(authentication)) {
            return "redirect:/";
        }

        UserResource user = (UserResource) authentication.getDetails();
        if (user.hasMoreThanOneRoleOf(Role.ASSESSOR, Role.APPLICANT, Role.STAKEHOLDER)) {
            return "redirect:/roleSelection";
        }

        return getRedirectUrlForUser(user);
    }

    @GetMapping("/roleSelection")
    public String selectRole(Model model,
                             @ModelAttribute(name = "form", binding = false) RoleSelectionForm form) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserResource user = (UserResource) authentication.getDetails();
        if (unauthenticated(authentication) || (!user.hasMoreThanOneRoleOf(Role.ASSESSOR, Role.APPLICANT, Role.STAKEHOLDER))){
            return "redirect:/";
        }

        return doViewRoleSelection(model, user);
    }

    @PostMapping("/roleSelection")
    public String processRole(Model model,
                              UserResource user,
                              @Valid @ModelAttribute("form") RoleSelectionForm form,
                              BindingResult bindingResult,
                              ValidationHandler validationHandler,
                              HttpServletResponse response) {

        Supplier<String> failureView = () -> doViewRoleSelection(model, user);

        return validationHandler.failNowOrSucceedWith(failureView, () -> {
            ValidationMessages validationMessages = new ValidationMessages(bindingResult);
            cookieUtil.saveToCookie(response, "role", form.getSelectedRole().getName());
            return validationHandler.addAnyErrors(validationMessages, fieldErrorsToFieldErrors(), asGlobalErrors()).
                    failNowOrSucceedWith(failureView, () -> redirectToChosenDashboard(user, form.getSelectedRole().getName()));
        });
    }

    private String doViewRoleSelection(Model model, UserResource user) {
        RoleSelectionViewModel roleSelectionViewModel = new RoleSelectionViewModel(user);

        if (roleSelectionViewModel.getAcceptedRoles().contains(Role.APPLICANT)) {
            roleSelectionViewModel.setRoleDescription("Manage your applications and projects.");
        }
        if (roleSelectionViewModel.getAcceptedRoles().contains(Role.ASSESSOR)) {
            roleSelectionViewModel.setRoleDescription("Review the applications you have been invited to assess.");
        }
        if (roleSelectionViewModel.getAcceptedRoles().contains(Role.STAKEHOLDER)) {
            roleSelectionViewModel.setRoleDescription("View the competitions you have been invited to oversee.");
        }

        model.addAttribute("model", roleSelectionViewModel);
        return "login/multiple-user-choice";
    }

    private String redirectToChosenDashboard(UserResource user, String role) {
        String url = user.getRoles().stream().filter(roleResource -> roleResource.getName().equals(role)).findFirst().get().getUrl();
        return format("redirect:/%s", url);
    }

    private static boolean unauthenticated(Authentication authentication) {
        return authentication == null || !authentication.isAuthenticated() || authentication.getDetails() == null;
    }
}
