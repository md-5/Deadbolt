package com.daemitus.deadbolt.config;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.nodes.*;

public class BukkitConstructor extends Constructor {

    private transient Plugin plugin;

    public BukkitConstructor(Class<?> clazz, Plugin plugin) {
        super(clazz);
        this.plugin = plugin;
        yamlClassConstructors.put(NodeId.scalar, new ConstructBukkitScalar());
        yamlClassConstructors.put(NodeId.mapping, new ConstructBukkitMapping());
    }

    private class ConstructBukkitScalar extends ConstructScalar {
    }

    private class ConstructBukkitMapping extends ConstructMapping {

        @Override
        @SuppressWarnings("unchecked")
        protected Object constructJavaBean2ndStep(MappingNode node, Object object) {
            Map<Class<? extends Object>, TypeDescription> typeDefinitions;
            try {
                Field typeDefField = Constructor.class.getDeclaredField("typeDefinitions");
                typeDefField.setAccessible(true);
                typeDefinitions = (Map<Class<? extends Object>, TypeDescription>) typeDefField.get((Constructor) BukkitConstructor.this);
                if (typeDefinitions == null) {
                    throw new NullPointerException();
                }
            } catch (Exception ex) {
                throw new YAMLException(ex);
            }
            flattenMapping(node);
            Class<? extends Object> beanType = node.getType();
            List<NodeTuple> nodeValue = node.getValue();
            for (NodeTuple tuple : nodeValue) {
                ScalarNode keyNode;
                if (tuple.getKeyNode() instanceof ScalarNode) {
                    // key must be scalar
                    keyNode = (ScalarNode) tuple.getKeyNode();
                } else {
                    throw new YAMLException("Keys must be scalars but found: " + tuple.getKeyNode());
                }
                Node valueNode = tuple.getValueNode();
                // keys can only be Strings
                keyNode.setType(String.class);
                String key = (String) constructObject(keyNode);
                try {
                    Property property;
                    try {
                        property = getProperty(beanType, key);
                    } catch (YAMLException e) {
                        continue;
                    }
                    valueNode.setType(property.getType());
                    TypeDescription memberDescription = typeDefinitions.get(beanType);
                    boolean typeDetected = false;
                    if (memberDescription != null) {
                        switch (valueNode.getNodeId()) {
                            case sequence:
                                SequenceNode snode = (SequenceNode) valueNode;
                                Class<? extends Object> memberType = memberDescription.getListPropertyType(key);
                                if (memberType != null) {
                                    snode.setListType(memberType);
                                    typeDetected = true;
                                } else if (property.getType().isArray()) {
                                    snode.setListType(property.getType().getComponentType());
                                    typeDetected = true;
                                }
                                break;
                            case mapping:
                                MappingNode mnode = (MappingNode) valueNode;
                                Class<? extends Object> keyType = memberDescription.getMapKeyType(key);
                                if (keyType != null) {
                                    mnode.setTypes(keyType, memberDescription.getMapValueType(key));
                                    typeDetected = true;
                                }
                                break;
                        }
                    }
                    if (!typeDetected && valueNode.getNodeId() != NodeId.scalar) {
                        // only if there is no explicit TypeDescription
                        Class<?>[] arguments = property.getActualTypeArguments();
                        if (arguments != null) {
                            // type safe (generic) collection may contain the
                            // proper class
                            if (valueNode.getNodeId() == NodeId.sequence) {
                                Class<?> t = arguments[0];
                                SequenceNode snode = (SequenceNode) valueNode;
                                snode.setListType(t);
                            } else if (valueNode.getTag().equals(Tag.SET)) {
                                Class<?> t = arguments[0];
                                MappingNode mnode = (MappingNode) valueNode;
                                mnode.setOnlyKeyType(t);
                                mnode.setUseClassConstructor(true);
                            } else if (property.getType().isAssignableFrom(Map.class)) {
                                Class<?> ketType = arguments[0];
                                Class<?> valueType = arguments[1];
                                MappingNode mnode = (MappingNode) valueNode;
                                mnode.setTypes(ketType, valueType);
                                mnode.setUseClassConstructor(true);
                            } else {
                                // the type for collection entries cannot be
                                // detected
                            }
                        }
                    }
                    Object value = constructObject(valueNode);
                    property.set(object, value);
                } catch (Exception e) {
                    throw new YAMLException("Cannot create property=" + key + " for JavaBean="
                            + object + "; " + e.getMessage(), e);
                }
            }
            return object;
        }
    }

    @Override
    protected Class<?> getClassForNode(Node node) {
        Class<?> clazz;
        String name = node.getTag().getClassName();
        if (plugin == null) {
            clazz = super.getClassForNode(node);
        } else {
            JavaPluginLoader jpl = (JavaPluginLoader) plugin.getPluginLoader();
            clazz = jpl.getClassByName(name);
        }

        if (clazz == null) {
            throw new YAMLException("Class not found: " + name);
        } else {
            return clazz;
        }
    }
}
