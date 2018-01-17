package org.innovateuk.ifs.file.controller;

import org.innovateuk.ifs.commons.error.Error;
import org.innovateuk.ifs.file.resource.FileTypeCategories;
import org.junit.Test;

import java.util.List;

import static java.util.Collections.singletonList;
import static org.innovateuk.ifs.commons.error.Error.fieldError;
import static org.junit.Assert.assertArrayEquals;
import static org.springframework.http.HttpStatus.UNSUPPORTED_MEDIA_TYPE;

public class ValidMediaTypesFileUploadErrorTranslatorTest {

    private Error pdfOnlyErrorFromDataLayer = fieldError(null, "application/madeup", UNSUPPORTED_MEDIA_TYPE.name(),
            singletonList(FileTypeCategories.PDF.getMediaTypeString()));

    private Error spreadsheetOnlyErrorFromDataLayer = fieldError(null, "application/madeup", UNSUPPORTED_MEDIA_TYPE.name(),
            singletonList(FileTypeCategories.SPREADSHEET.getMediaTypeString()));

    private Error pdfOrSpreadsheetOnlyErrorFromDataLayer = fieldError(null, "application/madeup", UNSUPPORTED_MEDIA_TYPE.name(),
            singletonList(FileTypeCategories.SPREADSHEET.getMediaTypeString() + ", " + FileTypeCategories.PDF.getMediaTypeString()));

    private Error notDirectMatchingMediaTypesErrorFromDataLayer = fieldError(null, "application/madeup", UNSUPPORTED_MEDIA_TYPE.name(),
            singletonList(FileTypeCategories.PDF.getMediaTypeString() + ", application/nomatch"));


    @Test
    public void testPdfOnlyMessage() {
        assertSpecialisedMessageProduced(pdfOnlyErrorFromDataLayer, ValidMediaTypesFileUploadErrorTranslator.UNSUPPORTED_MEDIA_TYPE_PDF_ONLY_MESSAGE_KEY);
    }

    @Test
    public void testSpreadsheetOnlyMessage() {
        assertSpecialisedMessageProduced(spreadsheetOnlyErrorFromDataLayer, ValidMediaTypesFileUploadErrorTranslator.UNSUPPORTED_MEDIA_TYPE_SPREADSHEET_ONLY_MESSAGE_KEY);
    }

    @Test
    public void testPdfAndSpreadsheetOnlyMessage() {
        assertSpecialisedMessageProduced(pdfOrSpreadsheetOnlyErrorFromDataLayer, ValidMediaTypesFileUploadErrorTranslator.UNSUPPORTED_MEDIA_TYPE_PDF_OR_SPREADSHEET_ONLY_MESSAGE_KEY);
    }

    @Test
    public void testNoDirectMatchWithValidMediaTypes() {
        assertSpecialisedMessageProduced(notDirectMatchingMediaTypesErrorFromDataLayer, UNSUPPORTED_MEDIA_TYPE.name());
    }

    private void assertSpecialisedMessageProduced(Error errorFromDataLayer, String expectedErrorKey) {

        List<Error> errors = new ValidMediaTypesFileUploadErrorTranslator().translateFileUploadErrors(e -> "formInput[123]", singletonList(errorFromDataLayer));

        Error expectedSpecialisedError = fieldError("formInput[123]", "application/madeup", expectedErrorKey);

        assertArrayEquals(new Error[] {expectedSpecialisedError}, errors.toArray());
    }
}