package org.innovateuk.ifs.application.forms.sections.yourprojectlocation.viewmodel;

/**
 * View model to support project location page
 */
public class YourProjectLocationViewModel {

    private boolean readOnly;

    // for the view
    private String financesUrl;
    private String applicationName;
    // and for autosave url
    private long applicationId;
    private long sectionId;
    // for mark as complete
    private boolean open;
    private boolean complete;

    public YourProjectLocationViewModel(
            boolean complete,
            boolean readOnly,
            String financesUrl,
            String applicationName,
            long applicationId,
            long sectionId,
            boolean open) {

        this.complete = complete;
        this.readOnly = readOnly;
        this.financesUrl = financesUrl;
        this.applicationName = applicationName;
        this.applicationId = applicationId;
        this.sectionId = sectionId;
        this.open = open;
    }

    public boolean isComplete() {
        return complete;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public String getFinancesUrl() {
        return financesUrl;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public long getApplicationId() {
        return applicationId;
    }

    public long getSectionId() {
        return sectionId;
    }

    public boolean isOpen() {
        return open;
    }
}
