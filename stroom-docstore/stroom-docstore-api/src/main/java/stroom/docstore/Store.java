package stroom.docstore;

import stroom.docref.DocRef;
import stroom.docstore.shared.Doc;
import stroom.importexport.shared.ImportState;
import stroom.query.api.v2.DocRefInfo;
import stroom.util.shared.Message;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface Store<D extends Doc> extends DocumentActionHandler<D> {

    DocRef createDocument(String name);

    DocRef copyDocument(String originalUuid,
                        String copyUuid,
                        Map<String, String> otherCopiesByOriginalUuid);

    DocRef moveDocument(String uuid);

    DocRef renameDocument(String uuid, String name);

    void deleteDocument(String uuid);

    DocRefInfo info(String uuid);

    Set<DocRef> listDocuments();

    Map<DocRef, Set<DocRef>> getDependencies();

    DocRef importDocument(
            DocRef docRef,
            Map<String, byte[]> dataMap,
            ImportState importState,
            ImportState.ImportMode importMode);

    Map<String, byte[]> exportDocument(DocRef docRef,
                                       boolean omitAuditFields,
                                       List<Message> messageList);

    List<DocRef> list();

    List<DocRef> findByName(String name);

}
