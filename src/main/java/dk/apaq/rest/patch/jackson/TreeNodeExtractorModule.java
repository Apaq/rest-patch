package dk.apaq.rest.patch.jackson;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * A custom Jackson module that registers the {@link TreeNodeExtractorDeserializer} to capture and store
 * the root JSON tree node during deserialization.
 *
 * This module automatically wraps the default deserializer with the {@link TreeNodeExtractorDeserializer}
 * for all bean types, allowing interception of JSON tree nodes during the deserialization process.
 */
public class TreeNodeExtractorModule extends SimpleModule {

    /**
     * Configures this module by adding a custom {@link BeanDeserializerModifier} to the Jackson context.
     * The modifier replaces the default deserializer with the {@link TreeNodeExtractorDeserializer}, which
     * intercepts the deserialization process and stores the root JSON tree node in {@link TreeNodeHolder}.
     *
     * @param context The setup context for the module, allowing registration of custom deserializers.
     */
    @Override
    public void setupModule(SetupContext context) {
        super.setupModule(context);
        context.addBeanDeserializerModifier(new BeanDeserializerModifier() {
            @Override
            public JsonDeserializer<?> modifyDeserializer(DeserializationConfig config,
                                                          BeanDescription beanDesc,
                                                          JsonDeserializer<?> deserializer) {
                // Wrap the default deserializer with TreeNodeExtractorDeserializer to capture JSON tree nodes
                return new TreeNodeExtractorDeserializer(deserializer);
            }
        });
    }
}
