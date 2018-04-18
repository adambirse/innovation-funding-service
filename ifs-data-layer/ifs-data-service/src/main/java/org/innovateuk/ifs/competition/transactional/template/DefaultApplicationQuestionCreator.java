package org.innovateuk.ifs.competition.transactional.template;

import org.innovateuk.ifs.form.domain.GuidanceRow;
import org.innovateuk.ifs.form.domain.Question;
import org.innovateuk.ifs.competition.domain.Competition;
import org.innovateuk.ifs.form.domain.FormInput;
import org.innovateuk.ifs.form.domain.FormValidator;
import org.innovateuk.ifs.form.repository.FormValidatorRepository;
import org.innovateuk.ifs.form.resource.FormInputScope;
import org.innovateuk.ifs.form.resource.FormInputType;
import org.innovateuk.ifs.application.validator.NotEmptyValidator;
import org.innovateuk.ifs.application.validator.WordCountValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;

/**
 * Component that creates a Question object containing default values & validators.
 */
@Component
public class DefaultApplicationQuestionCreator {

    private static final Integer DEFAULT_APPLICANT_WORD_COUNT = 400;
    private static final Integer DEFAULT_FEEDBACK_WORD_COUNT = 100;
    private static final Integer DEFAULT_MAXIMUM_SCORE = 10;

    @Autowired
    private FormValidatorRepository formValidatorRepository;

    public Question buildQuestion(Competition competition) {
        FormValidator notEmptyValidator = formValidatorRepository.findByClazzNameIn(asList(NotEmptyValidator.class.getName(), NotEmptyValidator.OLD_PACKAGE_NAME));
        FormValidator wordCountValidator = formValidatorRepository.findByClazzNameIn(asList(WordCountValidator.class.getName(), NotEmptyValidator.OLD_PACKAGE_NAME));

        FormInput maxWordCountInput = buildApplicantTextInput(competition, notEmptyValidator, wordCountValidator);
        FormInput questionScoreInput = buildQuestionScoreInput(competition, notEmptyValidator);
        FormInput feedbackInput = buildFeedbackInput(competition, notEmptyValidator, wordCountValidator);
        FormInput appendixInput = buildAppendixInput(competition);

        Question question = new Question();
        question.setCompetition(competition);
        question.setMarkAsCompletedEnabled(true);
        question.setAssessorMaximumScore(DEFAULT_MAXIMUM_SCORE);
        question.setFormInputs(Arrays.asList(maxWordCountInput, questionScoreInput, feedbackInput, appendixInput));

        return question;
    }

    private FormInput buildApplicantTextInput(Competition competition, FormValidator notEmptyValidator, FormValidator wordCountValidator) {
        FormInput input = new FormInput();
        input.setWordCount(DEFAULT_APPLICANT_WORD_COUNT);
        input.setType(FormInputType.TEXTAREA);
        input.setCompetition(competition);
        input.setIncludedInApplicationSummary(true);
        input.setPriority(0);
        input.setScope(FormInputScope.APPLICATION);
        input.setActive(true);

        input.setInputValidators(Stream.of(notEmptyValidator, wordCountValidator).collect(Collectors.toSet()));

        return input;
    }

    private FormInput buildQuestionScoreInput(Competition competition, FormValidator notEmptyValidator) {
        FormInput input = new FormInput();
        input.setType(FormInputType.ASSESSOR_SCORE);
        input.setCompetition(competition);
        input.setIncludedInApplicationSummary(false);
        input.setPriority(0);
        input.setDescription("Feedback");
        input.setScope(FormInputScope.ASSESSMENT);
        input.setActive(true);

        input.setInputValidators(Stream.of(notEmptyValidator).collect(Collectors.toSet()));

        return input;
    }

    private FormInput buildAppendixInput(Competition competition) {
        FormInput input = new FormInput();
        input.setType(FormInputType.FILEUPLOAD);
        input.setCompetition(competition);
        input.setIncludedInApplicationSummary(true);
        input.setPriority(1);
        input.setScope(FormInputScope.APPLICATION);
        input.setActive(false);
        input.setGuidanceAnswer(null);
        input.setGuidanceTitle("What should I include in the appendix?");
        input.setDescription("Appendix");
        input.setAllowedFileTypes(null);

        return input;
    }

    private FormInput buildFeedbackInput(Competition competition, FormValidator notEmptyValidator, FormValidator wordCountValidator) {
        FormInput input = new FormInput();
        input.setWordCount(DEFAULT_FEEDBACK_WORD_COUNT);
        input.setType(FormInputType.TEXTAREA);
        input.setCompetition(competition);
        input.setIncludedInApplicationSummary(false);
        input.setPriority(0);
        input.setDescription("Feedback");
        input.setScope(FormInputScope.ASSESSMENT);
        input.setActive(true);

        GuidanceRow justificationRow1 = new GuidanceRow();
        justificationRow1.setPriority(4);
        justificationRow1.setSubject("1,2");
        GuidanceRow justificationRow2 = new GuidanceRow();
        justificationRow2.setPriority(3);
        justificationRow2.setSubject("3,4");
        GuidanceRow justificationRow3 = new GuidanceRow();
        justificationRow3.setPriority(2);
        justificationRow3.setSubject("5,6");
        GuidanceRow justificationRow4 = new GuidanceRow();
        justificationRow4.setPriority(1);
        justificationRow4.setSubject("7,8");
        GuidanceRow justificationRow5 = new GuidanceRow();
        justificationRow5.setPriority(0);
        justificationRow5.setSubject("9,10");

        input.setInputValidators(Stream.of(notEmptyValidator, wordCountValidator).collect(Collectors.toSet()));
        input.setGuidanceRows(Arrays.asList(justificationRow1, justificationRow2, justificationRow3, justificationRow4, justificationRow5));

        return input;
    }
}
