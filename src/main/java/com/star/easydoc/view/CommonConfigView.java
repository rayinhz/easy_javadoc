package com.star.easydoc.view;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.*;

import com.google.common.collect.Lists;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.ui.CollectionListModel;
import com.intellij.ui.ListCellRendererWrapper;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBList;
import com.star.easydoc.model.EasyJavadocConfiguration;
import com.star.easydoc.util.BeanUtil;
import com.star.easydoc.util.JsonUtil;
import com.star.easydoc.view.inner.WordMapAddView;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.BooleanUtils;

/**
 * @author wangchao
 * @date 2019/08/25
 */
public class CommonConfigView {
    private static final Logger LOGGER = Logger.getInstance(CommonConfigView.class);

    private EasyJavadocConfiguration config;
    private JPanel panel;
    private JPanel wordMapPanel;
    private JTextField authorTextField;
    private JTextField dateFormatTextField;
    private JPanel classPanel;
    private JPanel fieldPanel;
    private JLabel authorLabel;
    private JLabel dataFormatLabel;
    private JRadioButton simpleDocButton;
    private JRadioButton normalDocButton;
    private JLabel fieldDocLabel;
    private JPanel commonPanel;
    private JComboBox translatorBox;
    private JLabel translatorLabel;
    private JButton importButton;
    private JButton exportButton;
    private JBList<Entry<String, String>> typeMapList;

    public CommonConfigView(EasyJavadocConfiguration config) {
        this.config = config;
        refreshWordMap();

        simpleDocButton.addChangeListener(e -> {
            JRadioButton button = (JRadioButton)e.getSource();
            if (button.isSelected()) {
                normalDocButton.setSelected(false);
            } else {
                normalDocButton.setSelected(true);
            }
        });

        normalDocButton.addChangeListener(e -> {
            JRadioButton button = (JRadioButton)e.getSource();
            if (button.isSelected()) {
                simpleDocButton.setSelected(false);
            } else {
                simpleDocButton.setSelected(true);
            }
        });

        importButton.addActionListener(event -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            int res = chooser.showOpenDialog(new JLabel());
            if (JFileChooser.APPROVE_OPTION != res) {
                return;
            }
            File file = chooser.getSelectedFile();
            if (file == null) {
                return;
            }
            try {
                String json = FileUtils.readFileToString(file);
                EasyJavadocConfiguration configuration = JsonUtil.fromJson(json, EasyJavadocConfiguration.class);
                if (configuration == null) {
                    throw new IllegalArgumentException("文件中内容格式不正确，请确认是否是json格式");
                }
                BeanUtil.copyProperties(configuration, this.config);
                refresh();
            } catch (Exception e) {
                LOGGER.error("读取文件异常", e);
            }
        });

        exportButton.addActionListener(event -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int res = chooser.showSaveDialog(new JLabel());
            if (JFileChooser.APPROVE_OPTION != res) {
                return;
            }
            File file = chooser.getSelectedFile();
            if (file == null) {
                return;
            }
            try {
                File targetFile = new File(file.getAbsolutePath() + "/easy_javadoc.json");
                FileUtils.write(targetFile, JsonUtil.toJson(this.config), StandardCharsets.UTF_8.name());
            } catch (Exception e) {
                LOGGER.error("写入文件异常", e);
            }
        });
    }

    private void createUIComponents() {
        typeMapList = new JBList<>(new CollectionListModel<>(Lists.newArrayList()));
        typeMapList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        typeMapList.setCellRenderer(new ListCellRendererWrapper<Entry<String, String>>() {
            @Override
            public void customize(JList list, Entry<String, String> value, int index, boolean selected, boolean hasFocus) {
                setText(value.getKey() + " -> " + value.getValue());
            }
        });

        typeMapList.setEmptyText("请添加单词映射");
        typeMapList.setSelectedIndex(0);
        ToolbarDecorator toolbarDecorator = ToolbarDecorator.createDecorator(typeMapList);
        toolbarDecorator.setAddAction(button -> {
            WordMapAddView wordMapAddView = new WordMapAddView();
            if (wordMapAddView.showAndGet()) {
                if (config != null) {
                    Entry<String, String> entry = wordMapAddView.getMapping();
                    config.getWordMap().put(entry.getKey(), entry.getValue());
                    refreshWordMap();
                }
            }
        });
        toolbarDecorator.setRemoveAction(anActionButton -> {
            if (config != null) {
                Map<String, String> typeMap = config.getWordMap();
                typeMap.remove(typeMapList.getSelectedValue().getKey());
                refreshWordMap();
            }
        });
        wordMapPanel = toolbarDecorator.createPanel();
    }

    public void refresh() {
        if (BooleanUtils.isTrue(config.getSimpleFieldDoc())) {
            setSimpleDocButton(true);
            setNormalDocButton(false);
        } else {
            setSimpleDocButton(false);
            setNormalDocButton(true);
        }
        setAuthorTextField(config.getAuthor());
        setDateFormatTextField(config.getDateFormat());
        setTranslatorBox(config.getTranslator());
        refreshWordMap();
    }

    private void refreshWordMap() {
        if (null != config && config.getWordMap() != null) {
            typeMapList.setModel(new CollectionListModel<>(Lists.newArrayList(config.getWordMap().entrySet())));
        }
    }

    public JComboBox getTranslatorBox() {
        return translatorBox;
    }

    public JComponent getComponent() {
        return panel;
    }

    public JTextField getAuthorTextField() {
        return authorTextField;
    }

    public JTextField getDateFormatTextField() {
        return dateFormatTextField;
    }

    public JRadioButton getSimpleDocButton() {
        return simpleDocButton;
    }

    public JRadioButton getNormalDocButton() {
        return normalDocButton;
    }

    public void setSimpleDocButton(boolean b) {
        simpleDocButton.setSelected(b);
    }

    public void setNormalDocButton(boolean b) {
        normalDocButton.setSelected(b);
    }

    public void setAuthorTextField(String author) {
        authorTextField.setText(author);
    }

    public void setTranslatorBox(String translator) {
        translatorBox.setSelectedItem(translator);
    }

    public void setDateFormatTextField(String dateFormat) {
        dateFormatTextField.setText(dateFormat);
    }
}
