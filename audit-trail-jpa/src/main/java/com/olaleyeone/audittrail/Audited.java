package com.olaleyeone.audittrail;

import com.olaleyeone.audittrail.embeddable.Audit;

public interface Audited {

    Audit getAudit();
}
