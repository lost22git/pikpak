package lost.pikpak.client.model;

import io.soabase.recordbuilder.core.RecordBuilderFull;
import lost.pikpak.client.enums.SortOrder;
import lost.pikpak.client.enums.ThumbnailSize;

import java.util.ArrayList;

@RecordBuilderFull
public record FileListParam(
    ThumbnailSize thumbnailSize,
    int limit,
    String pageToken,
    String parentId,
    boolean withAudit,
    SortOrder sortOrder

) {

    public String asQueryString() {
        var list = new ArrayList<String>();
        list.add("limit=" + limit);
        list.add("with_audit=" + withAudit);
        if (pageToken != null) {
            list.add("page_token=" + pageToken);
        }
        if (parentId != null) {
            list.add("parent_id=" + parentId);
        }
        if (thumbnailSize != null) {
            list.add("thumbnail_size=" + thumbnailSize.getValue());
        }
        if (sortOrder != null) {
            list.add("order=" + sortOrder.getValue());
        }
        return String.join("&", list);
    }
}
