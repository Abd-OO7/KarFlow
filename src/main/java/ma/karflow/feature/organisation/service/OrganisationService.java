package ma.karflow.feature.organisation.service;

import ma.karflow.feature.organisation.dto.*;

import java.util.UUID;

public interface OrganisationService {

    OrganisationResponse getMyOrganisation();

    OrganisationResponse createOrganisation(OrganisationRequest request);

    OrganisationResponse updateOrganisation(OrganisationRequest request);

    void addCityToOrganisation(UUID cityId);

    void removeCityFromOrganisation(UUID cityId);
}
