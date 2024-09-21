package org.hibernate.omm.translate.translator.ast;

import org.bson.BsonWriter;

public record AstAscendingSortOrder() implements AstSortOrder {
    @Override
    public void render(final BsonWriter writer) {
        writer.writeInt32(1);
    }
}