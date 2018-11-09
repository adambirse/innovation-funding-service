package org.innovateuk.ifs.eugrant.scheduled;

import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.eugrant.EuGrantResource;
import org.innovateuk.ifs.eugrant.transactional.EuGrantService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.innovateuk.ifs.commons.error.CommonErrors.internalServerErrorError;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceFailure;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;
import static org.innovateuk.ifs.eugrant.builder.EuGrantResourceBuilder.newEuGrantResource;
import static org.innovateuk.ifs.service.ServiceFailureTestHelper.assertThatServiceFailureIs;
import static org.mockito.Mockito.*;

/**
 * TODO DW - document this class
 */
@RunWith(MockitoJUnitRunner.class)
public class GrantSaverTest {

    @InjectMocks
    private GrantSaver grantSaver;

    @Mock
    private EuGrantService euGrantServiceMock;

    @Test
    public void saveGrant() {

        EuGrantResource grantToSave = newEuGrantResource().build();
        EuGrantResource savedGrant = newEuGrantResource().withId(UUID.randomUUID()).build();
        EuGrantResource retrievedGrant = newEuGrantResource().withId(UUID.randomUUID()).withShortCode("adsf").build();

        when(euGrantServiceMock.create()).thenReturn(serviceSuccess(savedGrant));
        when(euGrantServiceMock.update(savedGrant.getId(), grantToSave)).thenReturn(serviceSuccess());
        when(euGrantServiceMock.findById(savedGrant.getId())).thenReturn(serviceSuccess(retrievedGrant));

        ServiceResult<EuGrantResource> saveResult = grantSaver.saveGrant(grantToSave);

        assertThat(saveResult.isSuccess()).isTrue();
        assertThat(saveResult.getSuccess()).isEqualTo((retrievedGrant));

        verify(euGrantServiceMock, times(1)).create();
        verify(euGrantServiceMock, times(1)).update(savedGrant.getId(), grantToSave);
        verify(euGrantServiceMock, times(1)).findById(savedGrant.getId());
    }

    @Test
    public void saveGrantFailureHandling() {

        EuGrantResource grantToSave = newEuGrantResource().build();
        EuGrantResource savedGrant = newEuGrantResource().withId(UUID.randomUUID()).build();

        when(euGrantServiceMock.create()).thenReturn(serviceSuccess(savedGrant));
        when(euGrantServiceMock.update(savedGrant.getId(), grantToSave)).thenReturn(serviceFailure(internalServerErrorError()));

        ServiceResult<EuGrantResource> saveResult = grantSaver.saveGrant(grantToSave);

        assertThatServiceFailureIs(saveResult, internalServerErrorError());

        verify(euGrantServiceMock, times(1)).create();
        verify(euGrantServiceMock, times(1)).update(savedGrant.getId(), grantToSave);
        verify(euGrantServiceMock, never()).findById(savedGrant.getId());
    }
}
