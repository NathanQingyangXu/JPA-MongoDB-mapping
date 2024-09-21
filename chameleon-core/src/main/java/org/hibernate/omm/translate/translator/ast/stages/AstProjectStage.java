package org.hibernate.omm.translate.translator.ast.stages;

import org.bson.BsonWriter;
import org.hibernate.omm.translate.translator.ast.AstNodeType;

import java.util.List;

public record AstProjectStage(List<AstProjectStageSpecification> specifications) implements AstStage {
    @Override
    public AstNodeType nodeType() {
        return AstNodeType.ProjectStage;
    }

    @Override
    public void render(final BsonWriter writer) {
        writer.writeStartDocument();
        writer.writeName("$project");
        writer.writeStartDocument();
        specifications.forEach(specification -> specification.render(writer));
        writer.writeEndDocument();
        writer.writeEndDocument();
    }
}