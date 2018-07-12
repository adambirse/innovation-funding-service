*** Settings ***
Documentation     INFUND-3715 - As an Assessor I need to declare any conflicts of interest so that Innovate UK does not assign me assessments that are inappropriate for me.
...
...               INFUND-5432 As an assessor I want to receive an alert to complete my profile when I log into my dashboard so...
...
...               INFUND-7060 As an assessor I can view my declaration of interest page so...
Suite Setup       The user logs-in in new browser    &{existing_assessor1_credentials}
Suite Teardown    The user closes the browser
Force Tags        Assessor
Resource          ../../../resources/defaultResources.robot

*** Test Cases ***
Back to the dashboard link
    [Documentation]    INFUND-3715
    ...
    ...    INFUND-5432
    ...
    ...    INFUND-7060
    Given The user should see the element    jQuery=.message-alert.govuk-!-margin-bottom-6 a:contains("your declaration of interest")    #this checks the alert message on the top of the page
    When the user clicks the button/link    jQuery=h2:contains("Your assessor details") + ul a:contains("your declaration of interest")
    And The user should see the text in the element    css=p:nth-child(4)    Not answered
    And The user should see the text in the element    css=p:nth-child(14)    Not answered
    And the user clicks the button/link    jQuery=a:contains("Assessor dashboard")
    Then the user should be redirected to the correct page    ${assessor_dashboard_url}

Server-side validations when No selected at yes/no
    [Documentation]    INFUND-3715  IFS-1947
    ...
    ...    INFUND-7060
    [Tags]    HappyPath
    Given the user clicks the button/link    jQuery=a:contains("your declaration of interest")
    When the user clicks the button/link     jQuery=button:contains("Save and return to your declaration of interest")
    Then the user should see the proper validation messages triggered

Server-side when Yes selected at yes/no
    [Documentation]    INFUND-3715
    [Tags]    HappyPath
    Given the user selects the radio button    hasAppointments    yes
    When the user clicks the button/link       jQuery=button:contains("Save and return to your declaration of interest")
    Then the user should see a field error     Please enter an organisation.
    And the user should see a field error      Please enter a position.
    And the user selects the radio button      hasAppointments    no
    When the user selects the radio button     hasFinancialInterests    Yes
    And the user selects the radio button      hasFamilyAffiliations    Yes
    And the user selects the radio button      hasFamilyFinancialInterests    Yes
    And the user clicks the button/link        jQuery=button:contains("Save and return to your declaration of interest")
    Then the user should see a field error     Please enter a relation.
    And the user should see a field error      Please enter an organisation.
    And the user should see a field error      Please enter a position.
    And the user should see a field error      Please enter your family's financial interests.
    And the user should see a field error      Please enter your financial interests.
    Then the user enters multiple strings into a text field  id=professionalAffiliations  a${SPACE}  101
    And the user should see a field error      Maximum word count exceeded. Please reduce your word count to 100.

Client-side validations
    [Documentation]    INFUND-3715
    [Tags]    HappyPath
    When the user correctly fills out the role, principle employer and accurate fields
    Then The user should not see the element    jQuery=span:contains("Please enter a principal employer.")
    And The user should not see the element     jQuery=span:contains("Please enter your role with your principal employer.")
    And The user should not see the element     jQuery=span:contains("Please enter your financial interests.")
    And The user should not see the element     jQuery=span:contains("Please enter your family's financial interests.")
    And The user should not see the element     jQuery=span:contains("Please tell us if any of your immediate family members have any appointments or directorships.")
    And The user should not see the element     jQuery=span:contains("Please tell us if any of your immediate family members have any other financial interests.")
    And The user should not see the element     jQuery=span:contains("You must agree that your account is accurate.")
    And the user should not see the element     jQuery=span:contains("Maximum word count exceeded. Please reduce your word count to 100.")

Successful save for the DOI form
    [Documentation]    INFUND-3715
    ...
    ...    INFUND-5432
    [Tags]
    When the user clicks the button/link    jQuery=button:contains("Save and return to your declaration of interest")
    Then the user should be redirected to the correct page    ${assessment_declaration_url}
    And the user should see the element    jQuery=h2:contains("Principal employer and role") ~ p:contains("University")
    And the user should see the element    jQuery=h2:contains("Principal employer and role") ~ p:contains("Professor")
    And the user should see the element    jQuery=h2:contains("Professional affiliations") ~ p:contains("Role x at Company y")
    And the user should see the element    jQuery=h2:contains("Other financial interests") ~ p:contains("finance int")
    And the user should see the element    jQuery=td:contains("Relation")
    And the user should see the element    jQuery=td:contains("Innovate")
    And the user should see the element    jQuery=td:contains("Director")
    And the user should see the element    jQuery=p:contains("My interests")
    When the user clicks the button/link   jQuery=a:contains("Assessor dashboard")
    Then the user should be redirected to the correct page    ${assessor_dashboard_url}
    And the user should not see the element    jQuery=.message-alert a:contains('your declaration of interest')    #his checks the alert message on the top od the page
    And the user clicks the button/link    jQuery=a:contains("your declaration of interest")
    And the user clicks the button/link    jQuery=a:contains("Edit")
    And the user should see the correct inputs in the declaration form

*** Keywords ***
the user correctly fills out the role, principle employer and accurate fields
    the user enters text to a text field    id=principalEmployer    University
    the user enters text to a text field    id=role    Professor
    the user enters text to a text field    id=professionalAffiliations    Role x at Company y
    the user enters text to a text field    id=financialInterests    finance int
    the user enters text to a text field    Id=familyAffiliations[0].relation    Relation
    the user enters text to a text field    id=familyAffiliations[0].organisation    Innovate
    the user enters text to a text field    id=familyAffiliations[0].position    Director
    the user enters text to a text field    id=familyFinancialInterests    My interests
    the user moves focus to the element    css=[for^="accurateAccount"]
    the user selects the checkbox    accurateAccount1
    focus    jQuery=button:contains("Save and return to your declaration of interest")
    Wait For Autosave

the user should see the correct inputs in the declaration form
    Textfield Value Should Be    id=principalEmployer    University
    Textfield Value Should Be    id=role    Professor
    Textarea Value Should Be    id=professionalAffiliations    Role x at Company y
    Textarea Value Should Be    id=financialInterests    finance int
    Textarea Value Should Be    id=familyFinancialInterests    My interests

the user should not see the validation error
    [Arguments]    ${ERROR_TEXT}
    Wait Until Page Contains Element Without Screenshots    css=.error-message
    Wait Until Page Contains Without Screenshots    ${ERROR_TEXT}

the user should see the proper validation messages triggered
    the user should see a field and summary error    Please enter a principal employer.
    the user should see a field and summary error    Please enter your role with your principal employer.
    the user should see a field and summary error    Please tell us if any of your immediate family members have any appointments or directorships.
    the user should see a field and summary error    Please tell us if any of your immediate family members have any other financial interests.
    the user should see a field and summary error    Please tell us if you have any other financial interests.
    the user should see a field and summary error    Please tell us if you have any appointments or directorships.
    the user should see a field and summary error    You must agree that your account is accurate.