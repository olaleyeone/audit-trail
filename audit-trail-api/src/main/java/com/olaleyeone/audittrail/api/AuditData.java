package com.olaleyeone.audittrail.api;

import java.util.Optional;

public interface AuditData {

    Optional<Object> getData();
    Optional<String> getTextValue();
}
