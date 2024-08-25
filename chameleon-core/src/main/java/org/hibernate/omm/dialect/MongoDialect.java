/*
 * Copyright 2008-present MongoDB, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hibernate.omm.dialect;

import org.bson.BsonDocument;
import org.bson.BsonInt32;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.model.FunctionContributions;
import org.hibernate.boot.model.TypeContributions;
import org.hibernate.boot.model.relational.Exportable;
import org.hibernate.boot.model.relational.SqlStringGenerationContext;
import org.hibernate.dialect.DatabaseVersion;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.jdbc.dialect.spi.DialectResolutionInfo;
import org.hibernate.internal.util.collections.ArrayHelper;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.Constraint;
import org.hibernate.mapping.Index;
import org.hibernate.mapping.Selectable;
import org.hibernate.mapping.Table;
import org.hibernate.omm.array.function.MongoArrayContainsFunction;
import org.hibernate.omm.array.function.MongoArrayIncludesFunction;
import org.hibernate.omm.ast.MQLAstTranslatorFactory;
import org.hibernate.omm.dialect.exporter.MongoIndexCommandUtil;
import org.hibernate.omm.type.ObjectIdJavaType;
import org.hibernate.omm.type.ObjectIdJdbcType;
import org.hibernate.omm.util.StringUtil;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.sql.ast.SqlAstTranslatorFactory;
import org.hibernate.sql.ast.spi.ParameterMarkerStrategy;
import org.hibernate.sql.ast.spi.SqlAppender;
import org.hibernate.tool.schema.spi.Exporter;
import org.hibernate.type.descriptor.jdbc.JdbcType;
import org.hibernate.type.spi.TypeConfiguration;

import static org.hibernate.type.SqlTypes.ARRAY;

/**
 * @author Nathan Xu
 * @since 1.0.0
 */
public class MongoDialect extends Dialect {

    public static final int MINIMUM_MONGODB_MAJOR_VERSION_SUPPORTED = 3;

    private static final DatabaseVersion MINIMUM_VERSION = DatabaseVersion.make(MINIMUM_MONGODB_MAJOR_VERSION_SUPPORTED);

    private static final class NO_OP_EXPORTER<T extends Exportable> implements Exporter<T> {

        @Override
        public String[] getSqlCreateStrings(final T exportable, final Metadata metadata, final SqlStringGenerationContext context) {
            return ArrayHelper.EMPTY_STRING_ARRAY;
        }

        @Override
        public String[] getSqlDropStrings(final T exportable, final Metadata metadata, final SqlStringGenerationContext context) {
            return ArrayHelper.EMPTY_STRING_ARRAY;
        }
    }

    ;

    public MongoDialect() {
        this(MINIMUM_VERSION);
    }

    public MongoDialect(final DatabaseVersion version) {
        super(version);
    }

    public MongoDialect(final DialectResolutionInfo dialectResolutionInfo) {
        super(dialectResolutionInfo);
    }

    @Override
    public SqlAstTranslatorFactory getSqlAstTranslatorFactory() {
        return new MQLAstTranslatorFactory();
    }

    @Override
    public void appendLiteral(final SqlAppender appender, final String literal) {
        appender.appendSql(StringUtil.writeStringHelper(literal));
    }

    @Override
    public boolean supportsNullPrecedence() {
        return false;
    }

    @Override
    public boolean supportsStandardArrays() {
        return true;
    }

    @Override
    public int getPreferredSqlTypeCodeForArray() {
        return ARRAY;
    }

    @Override
    public void contribute(final TypeContributions typeContributions, final ServiceRegistry serviceRegistry) {
        contributeTypes(typeContributions, serviceRegistry);
        TypeConfiguration typeConfiguration = typeContributions.getTypeConfiguration();
        typeConfiguration.getJavaTypeRegistry().addDescriptor(ObjectIdJavaType.getInstance());
        typeConfiguration.getJdbcTypeRegistry().addDescriptor(ObjectIdJdbcType.getInstance());
    }

    @Override
    public void initializeFunctionRegistry(final FunctionContributions functionContributions) {
        var functionRegistry = functionContributions.getFunctionRegistry();
        var typeConfiguration = functionContributions.getTypeConfiguration();
        functionRegistry.register("array_contains", new MongoArrayContainsFunction(typeConfiguration));
        functionRegistry.register("array_includes", new MongoArrayIncludesFunction(typeConfiguration));
    }

    @Override
    public ParameterMarkerStrategy getNativeParameterMarkerStrategy() {
        return MongoParameterMarkerStrategy.INSTANCE;
    }

    @Override
    public Exporter<Table> getTableExporter() {
        return new NO_OP_EXPORTER<>();
    }

    @Override
    public Exporter<Index> getIndexExporter() {
        return new Exporter<>() {
            @Override
            public String[] getSqlCreateStrings(final Index index, final Metadata metadata, final SqlStringGenerationContext context) {
                final var collectionName = index.getTable().getName();
                final var keys = new BsonDocument();
                for (Selectable selectable : index.getSelectables()) {
                    if (!selectable.isFormula()) {
                        keys.put(selectable.getText(), new BsonInt32(1));
                    }
                }
                return new String[]{
                        MongoIndexCommandUtil.getIndexCreationCommand(collectionName, index.getName(), keys, false).toJson()
                };
            }

            @Override
            public String[] getSqlDropStrings(final Index exportable, final Metadata metadata, final SqlStringGenerationContext context) {
                return ArrayHelper.EMPTY_STRING_ARRAY;
            }
        };
    }

    @Override
    public Exporter<Constraint> getUniqueKeyExporter() {
        return new Exporter<>() {
            @Override
            public String[] getSqlCreateStrings(final Constraint constraint, final Metadata metadata, final SqlStringGenerationContext context) {
                final var collectionName = constraint.getTable().getName();
                final var keys = new BsonDocument();
                for (Column column : constraint.getColumns()) {
                    keys.put(column.getName(), new BsonInt32(1));
                }
                return new String[]{
                        MongoIndexCommandUtil.getIndexCreationCommand(collectionName, constraint.getName(), keys, true).toJson()
                };
            }

            @Override
            public String[] getSqlDropStrings(final Constraint exportable, final Metadata metadata, final SqlStringGenerationContext context) {
                return ArrayHelper.EMPTY_STRING_ARRAY;
            }
        };
    }

    private static class MongoParameterMarkerStrategy implements ParameterMarkerStrategy {
        /**
         * Singleton access
         */
        public static final MongoParameterMarkerStrategy INSTANCE = new MongoParameterMarkerStrategy();

        @Override
        public String createMarker(int position, JdbcType jdbcType) {
            return "{$undefined: true}";
        }
    }
}
