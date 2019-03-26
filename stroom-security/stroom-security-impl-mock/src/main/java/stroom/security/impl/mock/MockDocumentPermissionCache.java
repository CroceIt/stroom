package stroom.security.impl.mock;

import stroom.security.api.DocumentPermissionCache;

public class MockDocumentPermissionCache implements DocumentPermissionCache {
    @Override
    public boolean hasDocumentPermission(final String documentType, final String documentUuid, final String permission) {
        return true;
    }
}
