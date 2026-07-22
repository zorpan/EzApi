package com.newx.ezapi.core.utils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.*;

/**
 * XML 工具类
 * <p>
 * 提供 XML 到 JSON（Map/List）的递归转换功能，
 * 用于将 WebService 返回的 SOAP XML 响应转换为统一的 JSON 格式。
 * </p>
 */
public class XmlUtils {

    private XmlUtils() {
        // 工具类，禁止实例化
    }

    /**
     * 将 XML 字符串解析并转换为 Map 结构
     *
     * @param xml XML 字符串
     * @return 转换后的 Map 结构
     * @throws Exception 解析异常
     */
    public static Map<String, Object> xmlToMap(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        // 禁用外部实体解析，防止 XXE 攻击
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new InputSource(new StringReader(xml)));

        Element root = document.getDocumentElement();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put(root.getNodeName(), elementToObject(root));
        return result;
    }

    /**
     * 将 XML 元素递归转换为 Java 对象（Map、List 或 String）
     */
    @SuppressWarnings("unchecked")
    private static Object elementToObject(Element element) {
        NodeList childNodes = element.getChildNodes();
        Map<String, Object> nodeMap = new LinkedHashMap<>();

        // 处理属性
        if (element.hasAttributes()) {
            for (int i = 0; i < element.getAttributes().getLength(); i++) {
                Node attr = element.getAttributes().item(i);
                nodeMap.put("@" + attr.getNodeName(), attr.getNodeValue());
            }
        }

        // 收集子节点
        List<Node> elementChildren = new ArrayList<>();
        StringBuilder textContent = new StringBuilder();
        boolean hasElementChild = false;

        for (int i = 0; i < childNodes.getLength(); i++) {
            Node child = childNodes.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                elementChildren.add(child);
                hasElementChild = true;
            } else if (child.getNodeType() == Node.TEXT_NODE
                    || child.getNodeType() == Node.CDATA_SECTION_NODE) {
                String text = child.getNodeValue().trim();
                if (!text.isEmpty()) {
                    if (textContent.length() > 0) {
                        textContent.append(" ");
                    }
                    textContent.append(text);
                }
            }
        }

        // 如果有子元素，递归处理
        if (hasElementChild) {
            // 按标签名分组，处理同名兄弟元素为数组
            Map<String, List<Object>> grouped = new LinkedHashMap<>();
            for (Node child : elementChildren) {
                Element childElement = (Element) child;
                String childName = childElement.getNodeName();
                Object childValue = elementToObject(childElement);

                grouped.computeIfAbsent(childName, k -> new ArrayList<>()).add(childValue);
            }

            // 将分组结果放入 nodeMap
            for (Map.Entry<String, List<Object>> entry : grouped.entrySet()) {
                if (entry.getValue().size() == 1) {
                    // 只有一个子元素，直接放入
                    nodeMap.put(entry.getKey(), entry.getValue().get(0));
                } else {
                    // 多个同名子元素，放入数组
                    nodeMap.put(entry.getKey(), entry.getValue());
                }
            }
        }

        // 如果有文本内容
        String text = textContent.toString();
        if (!text.isEmpty()) {
            if (nodeMap.isEmpty()) {
                // 纯文本节点
                return text;
            } else {
                // 混合内容：既有子元素又有文本，用 #text 键存储文本
                nodeMap.put("#text", text);
            }
        }

        // 如果只有一个 @ 属性键且没有子元素，简化为属性值
        if (nodeMap.size() == 1 && nodeMap.containsKey("#text")) {
            return nodeMap.get("#text");
        }

        return nodeMap;
    }
}
