package flowershop.gui.Tab;

import flowershop.dao.FlowerDAO;
import flowershop.gui.Dialog.AddEditFlowerDialog;
import flowershop.models.Flower;
import flowershop.models.Flower.FlowerType;
import flowershop.models.Flower.FreshnessLevel;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FlowersTabTest {

    @Mock
    private FlowerDAO mockFlowerDAO;

    private FlowersTab flowersTab;

    @Mock
    private JTable mockItemsTable;
    @Mock
    private DefaultTableModel mockTableModel;
    @Mock
    private TableColumnModel mockTableColumnModel;
    @Mock
    private TableColumn mockTableColumn;
    @Mock
    private JProgressBar mockStockLevelBar;

    @Mock
    private JComboBox<FlowerType> mockTypeFilterCombo;
    @Mock
    private JComboBox<FreshnessLevel> mockFreshnessFilterCombo;
    @Mock
    private JSpinner mockMinPriceSpinner;
    @Mock
    private JSpinner mockMaxPriceSpinner;
    @Mock
    private JSpinner mockMinStemSpinner;
    @Mock
    private JSpinner mockMaxStemSpinner;
    @Mock
    private JCheckBox mockPottedCheckBox;
    @Mock
    private JButton mockClearFiltersButton;
    @Mock
    private JButton mockAddButton;
    @Mock
    private JButton mockEditButton;

    private JTextField searchField;

    @BeforeAll
    static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "true");
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("WARNING: Could not set LookAndFeel for tests: " + e.getMessage());
        }
    }

    @AfterAll
    static void resetHeadlessMode() {
        System.clearProperty("java.awt.headless");
    }

    @BeforeEach
    void setUp() {
        flowersTab = new FlowersTab();

        replaceFieldWithReflection(flowersTab, "itemDAO", mockFlowerDAO, AbstractItemTab.class);

        replaceFieldWithReflection(flowersTab, "itemsTable", mockItemsTable, AbstractItemTab.class);
        replaceFieldWithReflection(flowersTab, "tableModel", mockTableModel, AbstractItemTab.class);
        replaceFieldWithReflection(flowersTab, "stockLevelBar", mockStockLevelBar, AbstractItemTab.class);

        lenient().when(mockItemsTable.getColumnModel()).thenReturn(mockTableColumnModel);
        lenient().when(mockTableColumnModel.getColumn(anyInt())).thenReturn(mockTableColumn);

        replaceFieldWithReflection(flowersTab, "typeFilterCombo", mockTypeFilterCombo, FlowersTab.class);
        replaceFieldWithReflection(flowersTab, "freshnessFilterCombo", mockFreshnessFilterCombo, FlowersTab.class);
        replaceFieldWithReflection(flowersTab, "minPriceSpinner", mockMinPriceSpinner, FlowersTab.class);
        replaceFieldWithReflection(flowersTab, "maxPriceSpinner", mockMaxPriceSpinner, FlowersTab.class);
        replaceFieldWithReflection(flowersTab, "minStemSpinner", mockMinStemSpinner, FlowersTab.class);
        replaceFieldWithReflection(flowersTab, "maxStemSpinner", mockMaxStemSpinner, FlowersTab.class);
        replaceFieldWithReflection(flowersTab, "pottedCheckBox", mockPottedCheckBox, FlowersTab.class);

        replaceFieldWithReflection(flowersTab, "clearFiltersButton", mockClearFiltersButton, AbstractItemTab.class);
        replaceFieldWithReflection(flowersTab, "addButton", mockAddButton, AbstractItemTab.class);
        replaceFieldWithReflection(flowersTab, "editButton", mockEditButton, AbstractItemTab.class);

        try {
            Field searchFieldRef = AbstractItemTab.class.getDeclaredField("searchField");
            searchFieldRef.setAccessible(true);
            searchField = (JTextField) searchFieldRef.get(flowersTab);
            searchField.setText("");
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail("Could not access searchField: " + e.getMessage());
        }

        lenient().when(mockFlowerDAO.getAllFlowers()).thenReturn(new ArrayList<>());
        lenient().when(mockTableModel.getRowCount()).thenReturn(0);

        lenient().when(mockTypeFilterCombo.getSelectedItem()).thenReturn(null);
        lenient().when(mockFreshnessFilterCombo.getSelectedItem()).thenReturn(null);
        lenient().when(mockMinPriceSpinner.getValue()).thenReturn(0.0);
        lenient().when(mockMaxPriceSpinner.getValue()).thenReturn(500.0);
        lenient().when(mockMinStemSpinner.getValue()).thenReturn(0);
        lenient().when(mockMaxStemSpinner.getValue()).thenReturn(100);
        lenient().when(mockPottedCheckBox.isSelected()).thenReturn(false);
    }

    private void replaceFieldWithReflection(Object targetObject, String fieldName, Object newValue, Class<?> targetClass) {
        try {
            Field field = targetClass.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(targetObject, newValue);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail("Could not set field '" + fieldName + "' in " + targetClass.getSimpleName() + ": " + e.getMessage());
        }
    }

    @Test
    void constructor_initializesDAO() {
        try {
            Field daoField = AbstractItemTab.class.getDeclaredField("itemDAO");
            daoField.setAccessible(true);
            assertSame(mockFlowerDAO, daoField.get(flowersTab));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail("Could not access itemDAO field: " + e.getMessage());
        }
    }

    @Test
    void getTabTitle_returnsCorrectTitle() {
        assertEquals("Управління квітами", flowersTab.getTabTitle());
    }

    @Test
    void getColumnNames_returnsCorrectNames() {
        assertArrayEquals(new String[]{"ID", "Назва", "Колір", "Ціна", "Свіжість", "Стебло(см)", "Країна", "В горщику", "На складі"}, flowersTab.getColumnNames());
    }

    @Test
    void getColumnClasses_returnsCorrectClasses() {
        assertArrayEquals(new Class<?>[]{Integer.class, String.class, String.class, Double.class, String.class, Integer.class, String.class, String.class, Integer.class}, flowersTab.getColumnClasses());
    }

    @Test
    void getRowDataForItem_returnsCorrectData() {
        Flower flower = new Flower(FlowerType.ROSE, 25.50, 90, 60, "Червона", "Нідерланди", false, "rose.jpg", 100);
        flower.setId(1);

        Object[] rowData = flowersTab.getRowDataForItem(flower);
        assertArrayEquals(new Object[]{
                1,
                "Троянда",
                "Червона",
                25.50,
                "Дуже висока (90%)",
                60,
                "Нідерланди",
                "Ні",
                100
        }, rowData);

        Flower pottedFlower = new Flower(FlowerType.ORCHID, 150.0, 85, 30, "Біла", "Таїланд", true, "orchid.jpg", 20);
        pottedFlower.setId(2);
        Object[] pottedRowData = flowersTab.getRowDataForItem(pottedFlower);
        assertEquals("Так", pottedRowData[7]);
    }

    @Test
    void configureTableColumnWidths_setsPreferredWidthsAndRenderers() {
        TableColumn col0 = mock(TableColumn.class); TableColumn col1 = mock(TableColumn.class);
        TableColumn col2 = mock(TableColumn.class); TableColumn col3 = mock(TableColumn.class);
        TableColumn col4 = mock(TableColumn.class); TableColumn col5 = mock(TableColumn.class);
        TableColumn col6 = mock(TableColumn.class); TableColumn col7 = mock(TableColumn.class);
        TableColumn col8 = mock(TableColumn.class);

        when(mockTableColumnModel.getColumn(0)).thenReturn(col0); when(mockTableColumnModel.getColumn(1)).thenReturn(col1);
        when(mockTableColumnModel.getColumn(2)).thenReturn(col2); when(mockTableColumnModel.getColumn(3)).thenReturn(col3);
        when(mockTableColumnModel.getColumn(4)).thenReturn(col4); when(mockTableColumnModel.getColumn(5)).thenReturn(col5);
        when(mockTableColumnModel.getColumn(6)).thenReturn(col6); when(mockTableColumnModel.getColumn(7)).thenReturn(col7);
        when(mockTableColumnModel.getColumn(8)).thenReturn(col8);

        flowersTab.configureTableColumnWidths();

        verify(col0).setPreferredWidth(30); verify(col1).setPreferredWidth(120);
        verify(col2).setPreferredWidth(80); verify(col3).setPreferredWidth(70);
        verify(col4).setPreferredWidth(150); verify(col5).setPreferredWidth(80);
        verify(col6).setPreferredWidth(100); verify(col7).setPreferredWidth(80);
        verify(col8).setPreferredWidth(70);

        verify(col0).setCellRenderer(any(DefaultTableCellRenderer.class));
        verify(col5).setCellRenderer(any(DefaultTableCellRenderer.class));
        verify(col7).setCellRenderer(any(DefaultTableCellRenderer.class));
        verify(col8).setCellRenderer(any(DefaultTableCellRenderer.class));
        verify(col3).setCellRenderer(any(DefaultTableCellRenderer.class));
    }

    @Test
    void createFilterPanel_createsPanelWithRows() {
        FlowersTab realTab = new FlowersTab();
        JPanel filterPanel = realTab.createFilterPanel();
        assertNotNull(filterPanel);
        assertTrue(filterPanel.getLayout() instanceof BoxLayout);
        assertEquals(2, filterPanel.getComponentCount());

        Component row1Comp = filterPanel.getComponent(0);
        assertTrue(row1Comp instanceof JPanel);
        JPanel row1Panel = (JPanel) row1Comp;
        assertTrue(Arrays.stream(row1Panel.getComponents()).anyMatch(c -> c instanceof JLabel && ((JLabel)c).getText().equals("Пошук:")));
        assertTrue(Arrays.stream(row1Panel.getComponents()).anyMatch(c -> c instanceof JTextField));
        assertTrue(Arrays.stream(row1Panel.getComponents()).anyMatch(c -> c instanceof JLabel && ((JLabel)c).getText().equals("Тип:")));
        assertTrue(Arrays.stream(row1Panel.getComponents()).anyMatch(c -> c instanceof JComboBox));
        assertTrue(Arrays.stream(row1Panel.getComponents()).anyMatch(c -> c instanceof JLabel && ((JLabel)c).getText().equals("Свіжість:")));
        assertTrue(Arrays.stream(row1Panel.getComponents()).anyMatch(c -> c instanceof JComboBox));

        Component row2Comp = filterPanel.getComponent(1);
        assertTrue(row2Comp instanceof JPanel);
        JPanel row2Panel = (JPanel) row2Comp;
        assertTrue(Arrays.stream(row2Panel.getComponents()).anyMatch(c -> c instanceof JPanel && ((JPanel)c).getBorder() instanceof TitledBorder && ((TitledBorder)((JPanel)c).getBorder()).getTitle().contains("Ціна")));
        assertTrue(Arrays.stream(row2Panel.getComponents()).anyMatch(c -> c instanceof JPanel && ((JPanel)c).getBorder() instanceof TitledBorder && ((TitledBorder)((JPanel)c).getBorder()).getTitle().contains("Стебло (см)")));
        assertTrue(Arrays.stream(row2Panel.getComponents()).anyMatch(c -> c instanceof JCheckBox && ((JCheckBox)c).getText().equals("В горщику")));
        assertTrue(Arrays.stream(row2Panel.getComponents()).anyMatch(c -> c instanceof JButton && ((JButton)c).getText().equals("Очистити")));
    }

    @Test
    void filterItems_noFilters_returnsAll() {
        Flower f1 = new Flower(FlowerType.ROSE, 10, 90, 50, "Red", "NL", false, "", 10);
        Flower f2 = new Flower(FlowerType.TULIP, 5, 80, 30, "Yellow", "NL", false, "", 20);
        List<Flower> allFlowers = Arrays.asList(f1, f2);

        searchField.setText("");
        when(mockTypeFilterCombo.getSelectedItem()).thenReturn(null);
        when(mockFreshnessFilterCombo.getSelectedItem()).thenReturn(null);
        when(mockMinPriceSpinner.getValue()).thenReturn(0.0);
        when(mockMaxPriceSpinner.getValue()).thenReturn(500.0);
        when(mockMinStemSpinner.getValue()).thenReturn(0);
        when(mockMaxStemSpinner.getValue()).thenReturn(100);
        when(mockPottedCheckBox.isSelected()).thenReturn(false);

        List<Flower> filtered = flowersTab.filterItems(new ArrayList<>(allFlowers));
        assertEquals(2, filtered.size());
        assertTrue(filtered.containsAll(allFlowers));
    }

    @Test
    void filterItems_bySearchText_name() {
        Flower f1 = new Flower(FlowerType.ROSE, 10, 90, 50, "Red", "NL", false, "", 10);
        Flower f2 = new Flower(FlowerType.TULIP, 5, 80, 30, "Yellow", "PL", false, "", 20);
        List<Flower> allFlowers = Arrays.asList(f1, f2);
        searchField.setText("троянда");
        List<Flower> filtered = flowersTab.filterItems(new ArrayList<>(allFlowers));
        assertEquals(1, filtered.size());
        assertEquals(FlowerType.ROSE, filtered.get(0).getType());
    }

    @Test
    void filterItems_bySearchText_color() {
        Flower f1 = new Flower(FlowerType.ROSE, 10, 90, 50, "Червона", "NL", false, "", 10);
        Flower f2 = new Flower(FlowerType.TULIP, 5, 80, 30, "Жовтий", "PL", false, "", 20);
        List<Flower> allFlowers = Arrays.asList(f1, f2);
        searchField.setText("жовтий");
        List<Flower> filtered = flowersTab.filterItems(new ArrayList<>(allFlowers));
        assertEquals(1, filtered.size());
        assertEquals(FlowerType.TULIP, filtered.get(0).getType());
    }

    @Test
    void filterItems_bySearchText_country() {
        Flower f1 = new Flower(FlowerType.ROSE, 10, 90, 50, "Red", "Нідерланди", false, "", 10);
        Flower f2 = new Flower(FlowerType.TULIP, 5, 80, 30, "Yellow", "Польща", false, "", 20);
        List<Flower> allFlowers = Arrays.asList(f1, f2);
        searchField.setText("польща");
        List<Flower> filtered = flowersTab.filterItems(new ArrayList<>(allFlowers));
        assertEquals(1, filtered.size());
        assertEquals(FlowerType.TULIP, filtered.get(0).getType());
    }

    @Test
    void filterItems_byType() {
        Flower f1 = new Flower(FlowerType.ROSE, 10, 90, 50, "Red", "NL", false, "", 10);
        Flower f2 = new Flower(FlowerType.TULIP, 5, 80, 30, "Yellow", "PL", false, "", 20);
        List<Flower> allFlowers = Arrays.asList(f1, f2);
        when(mockTypeFilterCombo.getSelectedItem()).thenReturn(FlowerType.TULIP);
        List<Flower> filtered = flowersTab.filterItems(new ArrayList<>(allFlowers));
        assertEquals(1, filtered.size());
        assertEquals(FlowerType.TULIP, filtered.get(0).getType());
    }

    @Test
    void filterItems_byFreshness() {
        Flower f1 = new Flower(FlowerType.ROSE, 10, 95, 50, "Red", "NL", false, "", 10);
        Flower f2 = new Flower(FlowerType.TULIP, 5, 80, 30, "Yellow", "PL", false, "", 20);
        List<Flower> allFlowers = Arrays.asList(f1, f2);
        when(mockFreshnessFilterCombo.getSelectedItem()).thenReturn(FreshnessLevel.HIGH);
        List<Flower> filtered = flowersTab.filterItems(new ArrayList<>(allFlowers));
        assertEquals(1, filtered.size());
        assertEquals(FlowerType.TULIP, filtered.get(0).getType());
    }

    @Test
    void filterItems_byPriceRange() {
        Flower f1 = new Flower(FlowerType.ROSE, 10.0, 90, 50, "Red", "NL", false, "", 10);
        Flower f2 = new Flower(FlowerType.LILY, 50.0, 85, 70, "White", "PL", false, "", 5);
        Flower f3 = new Flower(FlowerType.TULIP, 5.0, 80, 30, "Yellow", "NL", false, "", 20);
        List<Flower> allFlowers = Arrays.asList(f1, f2, f3);
        when(mockMinPriceSpinner.getValue()).thenReturn(8.0);
        when(mockMaxPriceSpinner.getValue()).thenReturn(12.0);
        List<Flower> filtered = flowersTab.filterItems(new ArrayList<>(allFlowers));
        assertEquals(1, filtered.size());
        assertEquals(FlowerType.ROSE, filtered.get(0).getType());
    }

    @Test
    void filterItems_byStemLengthRange() {
        Flower f1 = new Flower(FlowerType.ROSE, 10, 90, 50, "Red", "NL", false, "", 10);
        Flower f2 = new Flower(FlowerType.LILY, 50, 85, 70, "White", "PL", false, "", 5);
        Flower f3 = new Flower(FlowerType.TULIP, 5, 80, 30, "Yellow", "NL", false, "", 20);
        List<Flower> allFlowers = Arrays.asList(f1, f2, f3);
        when(mockMinStemSpinner.getValue()).thenReturn(40);
        when(mockMaxStemSpinner.getValue()).thenReturn(60);
        List<Flower> filtered = flowersTab.filterItems(new ArrayList<>(allFlowers));
        assertEquals(1, filtered.size());
        assertEquals(FlowerType.ROSE, filtered.get(0).getType());
    }

    @Test
    void filterItems_byPotted() {
        Flower f1 = new Flower(FlowerType.ROSE, 10, 90, 50, "Red", "NL", false, "", 10);
        Flower f2 = new Flower(FlowerType.ORCHID, 150, 85, 30, "White", "TH", true, "", 5);
        List<Flower> allFlowers = Arrays.asList(f1, f2);
        when(mockPottedCheckBox.isSelected()).thenReturn(true);
        List<Flower> filtered = flowersTab.filterItems(new ArrayList<>(allFlowers));
        assertEquals(1, filtered.size());
        assertEquals(FlowerType.ORCHID, filtered.get(0).getType());
    }

    @Test
    void filterItems_combinedFilters() {
        Flower f1 = new Flower(FlowerType.ROSE, 20.0, 95, 60, "Червона", "Голландія", false, "r.jpg", 10);
        Flower f2 = new Flower(FlowerType.LILY, 30.0, 80, 70, "Біла", "Польща", false, "l.jpg", 5);
        Flower f3 = new Flower(FlowerType.ROSE, 25.0, 70, 50, "Жовта", "Голландія", false, "yr.jpg", 8);
        Flower f4 = new Flower(FlowerType.ORCHID, 100.0, 90, 30, "Рожева", "Таїланд", true, "o.jpg", 3);

        List<Flower> allFlowers = Arrays.asList(f1, f2, f3, f4);

        searchField.setText("ро");
        when(mockTypeFilterCombo.getSelectedItem()).thenReturn(FlowerType.ROSE);
        when(mockFreshnessFilterCombo.getSelectedItem()).thenReturn(FreshnessLevel.VERY_HIGH);
        when(mockMinPriceSpinner.getValue()).thenReturn(15.0);
        when(mockMaxPriceSpinner.getValue()).thenReturn(28.0);
        when(mockMinStemSpinner.getValue()).thenReturn(55);
        when(mockMaxStemSpinner.getValue()).thenReturn(65);
        when(mockPottedCheckBox.isSelected()).thenReturn(false);

        List<Flower> filtered = flowersTab.filterItems(new ArrayList<>(allFlowers));
        assertEquals(1, filtered.size());
        assertSame(f1, filtered.get(0));
    }

    @Test
    void clearFilters_resetsFieldsAndRefreshes() {
        searchField.setText("test");
        flowersTab.clearFilters();

        assertEquals("", searchField.getText());
        verify(mockTypeFilterCombo).setSelectedIndex(0);
        verify(mockFreshnessFilterCombo).setSelectedIndex(0);
        verify(mockMinPriceSpinner).setValue(0);
        verify(mockMaxPriceSpinner).setValue(500);
        verify(mockMinStemSpinner).setValue(0);
        verify(mockMaxStemSpinner).setValue(100);
        verify(mockPottedCheckBox).setSelected(false);
        verify(mockTableModel, atLeastOnce()).setRowCount(0);
    }

    @Test
    void getAllItemsFromDAO_callsDAO() {
        List<Flower> expectedFlowers = Collections.singletonList(new Flower(FlowerType.OTHER,0,0,0));
        when(mockFlowerDAO.getAllFlowers()).thenReturn(expectedFlowers);
        List<Flower> actualFlowers = flowersTab.getAllItemsFromDAO();
        assertSame(expectedFlowers, actualFlowers);
        verify(mockFlowerDAO).getAllFlowers();
    }

    @Test
    void getItemByIdFromDAO_callsDAO() {
        Flower expectedFlower = new Flower(FlowerType.OTHER,0,0,0);
        expectedFlower.setId(1);
        when(mockFlowerDAO.getFlowerById(1)).thenReturn(expectedFlower);
        Flower actualFlower = flowersTab.getItemByIdFromDAO(1);
        assertSame(expectedFlower, actualFlower);
        verify(mockFlowerDAO).getFlowerById(1);
    }

    @Test
    void showAddEditDialog_addMode_showsDialogAndSaves() {
        ActionEvent mockEvent = mock(ActionEvent.class);
        when(mockEvent.getSource()).thenReturn(mockAddButton);

        Flower newFlower = new Flower(FlowerType.ROSE, 15, 90, 55, "Red", "NL", false, "new.jpg", 50);
        newFlower.setId(99);

        try (MockedConstruction<AddEditFlowerDialog> mockedDialogConstruction = Mockito.mockConstruction(
                AddEditFlowerDialog.class,
                (mockDialog, context) -> {
                    assertNull(context.arguments().get(1));
                    when(mockDialog.isSaved()).thenReturn(true);
                    when(mockDialog.getFlower()).thenReturn(newFlower);
                })) {

            flowersTab.showAddEditDialog(mockEvent);

            assertEquals(1, mockedDialogConstruction.constructed().size());
            AddEditFlowerDialog constructedDialog = mockedDialogConstruction.constructed().get(0);
            verify(constructedDialog).setVisible(true);

            verify(mockFlowerDAO).saveFlower(newFlower);
            verify(mockTableModel, atLeastOnce()).setRowCount(0);
        }
    }

    @Test
    void showAddEditDialog_editMode_showsDialogAndSaves() {
        ActionEvent mockEvent = mock(ActionEvent.class);
        when(mockEvent.getSource()).thenReturn(mockEditButton);

        Flower flowerToEdit = new Flower(FlowerType.LILY, 20, 80, 60, "White", "PL", false, "old.jpg", 30);
        flowerToEdit.setId(123);
        Flower editedFlower = new Flower(FlowerType.LILY, 22, 85, 65, "Pink", "PL", false, "edited.jpg", 25);
        editedFlower.setId(123);

        when(mockItemsTable.getSelectedRow()).thenReturn(0);
        when(mockItemsTable.convertRowIndexToModel(0)).thenReturn(0);
        lenient().when(mockTableModel.getRowCount()).thenReturn(1);
        lenient().when(mockTableModel.getValueAt(0, 0)).thenReturn(123);
        when(mockFlowerDAO.getFlowerById(123)).thenReturn(flowerToEdit);

        try (MockedConstruction<AddEditFlowerDialog> mockedDialogConstruction = Mockito.mockConstruction(
                AddEditFlowerDialog.class,
                (mockDialog, context) -> {
                    assertSame(flowerToEdit, context.arguments().get(1));
                    when(mockDialog.isSaved()).thenReturn(true);
                    when(mockDialog.getFlower()).thenReturn(editedFlower);
                })) {
            flowersTab.showAddEditDialog(mockEvent);
            verify(mockFlowerDAO).saveFlower(editedFlower);
            verify(mockTableModel, atLeastOnce()).setRowCount(0);
        }
    }

    @Test
    void showAddEditDialog_editMode_noSelection_showsWarning() {
        ActionEvent mockEvent = mock(ActionEvent.class);
        when(mockEvent.getSource()).thenReturn(mockEditButton);
        when(mockItemsTable.getSelectedRow()).thenReturn(-1);

        try (MockedStatic<JOptionPane> mockedOptionPane = Mockito.mockStatic(JOptionPane.class)) {
            flowersTab.showAddEditDialog(mockEvent);
            mockedOptionPane.verify(() -> JOptionPane.showMessageDialog(
                    eq(flowersTab),
                    eq("Будь ласка, оберіть квітку для редагування."),
                    eq("Попередження"),
                    eq(JOptionPane.WARNING_MESSAGE)
            ));
        }
    }

    @Test
    void showAddEditDialog_dialogCancelled_doesNotSave() {
        ActionEvent mockEvent = mock(ActionEvent.class);
        when(mockEvent.getSource()).thenReturn(mockAddButton);

        try (MockedConstruction<AddEditFlowerDialog> mockedDialogConstruction = Mockito.mockConstruction(
                AddEditFlowerDialog.class,
                (mockDialog, context) -> {
                    when(mockDialog.isSaved()).thenReturn(false);
                })) {
            flowersTab.showAddEditDialog(mockEvent);
            verify(mockFlowerDAO, never()).saveFlower(any(Flower.class));
        }
    }

    @Test
    void selectRowByFlowerId_flowerExists() {
        when(mockItemsTable.getRowCount()).thenReturn(1);
        when(mockItemsTable.convertRowIndexToModel(0)).thenReturn(0);
        Flower flowerInTable = new Flower(FlowerType.ROSE, 10, 90, 50, "Red", "NL", false, "", 10);
        flowerInTable.setId(777);
        lenient().when(mockTableModel.getRowCount()).thenReturn(1);
        lenient().when(mockTableModel.getValueAt(0, 0)).thenReturn(777);
        when(mockFlowerDAO.getFlowerById(777)).thenReturn(flowerInTable);
        lenient().when(mockItemsTable.getCellRect(anyInt(), anyInt(), anyBoolean())).thenReturn(new Rectangle(0,0,1,1));

        flowersTab.selectRowByFlowerId(777);

        verify(mockItemsTable).setRowSelectionInterval(0, 0);
        verify(mockItemsTable).scrollRectToVisible(any(Rectangle.class));
    }

    @Test
    void selectRowByFlowerId_flowerNotExists_doesNotSelect() {
        when(mockItemsTable.getRowCount()).thenReturn(1);
        when(mockItemsTable.convertRowIndexToModel(0)).thenReturn(0);
        Flower otherFlower = new Flower(FlowerType.TULIP, 5, 80, 30, "Yellow", "PL", false, "", 20);
        otherFlower.setId(111);
        lenient().when(mockTableModel.getRowCount()).thenReturn(1);
        lenient().when(mockTableModel.getValueAt(0,0)).thenReturn(111);
        when(mockFlowerDAO.getFlowerById(111)).thenReturn(otherFlower);

        flowersTab.selectRowByFlowerId(777);
        verify(mockItemsTable, never()).setRowSelectionInterval(anyInt(), anyInt());
    }

    @Test
    void selectRowByFlowerId_emptyTable_doesNotSelect() {
        when(mockItemsTable.getRowCount()).thenReturn(0);
        flowersTab.selectRowByFlowerId(777);
        verify(mockItemsTable, never()).setRowSelectionInterval(anyInt(), anyInt());
    }

    @Test
    void selectRowByFlowerId_itemInRowIsNull_doesNotSelect() {
        when(mockItemsTable.getRowCount()).thenReturn(1);
        when(mockItemsTable.convertRowIndexToModel(0)).thenReturn(0);
        lenient().when(mockTableModel.getRowCount()).thenReturn(1);
        lenient().when(mockTableModel.getValueAt(0,0)).thenReturn(777);
        when(mockFlowerDAO.getFlowerById(777)).thenReturn(null);

        flowersTab.selectRowByFlowerId(777);
        verify(mockItemsTable, never()).setRowSelectionInterval(anyInt(), anyInt());
    }

    @Test
    void deleteSelectedItem_noSelection_showsWarning() {
        when(mockItemsTable.getSelectedRow()).thenReturn(-1);
        try (MockedStatic<JOptionPane> mockedOptionPane = Mockito.mockStatic(JOptionPane.class)) {
            flowersTab.deleteSelectedItem(null);
            mockedOptionPane.verify(() -> JOptionPane.showMessageDialog(
                    eq(flowersTab),
                    eq("Будь ласка, оберіть квітку для видалення."),
                    eq("Попередження"),
                    eq(JOptionPane.WARNING_MESSAGE)
            ));
        }
    }

    @Test
    void deleteSelectedItem_confirmed_deletesAndShowsMessage() {
        when(mockItemsTable.getSelectedRow()).thenReturn(0);
        when(mockItemsTable.convertRowIndexToModel(0)).thenReturn(0);
        Flower flowerToDelete = new Flower(FlowerType.DAISY, 2, 70, 20, "White", "UA", false, "d.jpg", 50);
        flowerToDelete.setId(1);
        when(mockFlowerDAO.getFlowerById(1)).thenReturn(flowerToDelete);
        lenient().when(mockTableModel.getRowCount()).thenReturn(1);
        lenient().when(mockTableModel.getValueAt(0, 0)).thenReturn(1);

        try (MockedStatic<JOptionPane> mockedOptionPane = Mockito.mockStatic(JOptionPane.class)) {
            mockedOptionPane.when(() -> JOptionPane.showConfirmDialog(any(), anyString(), anyString(), eq(JOptionPane.YES_NO_OPTION), eq(JOptionPane.WARNING_MESSAGE)))
                    .thenReturn(JOptionPane.YES_OPTION);

            flowersTab.deleteSelectedItem(null);

            verify(mockFlowerDAO).deleteFlower(1);
            verify(mockTableModel, atLeastOnce()).setRowCount(0);

            mockedOptionPane.verify(() -> JOptionPane.showMessageDialog(
                    eq(flowersTab),
                    eq("Квітку \"" + flowerToDelete.getDisplayName() + "\" успішно видалено."),
                    eq("Видалення завершено"),
                    eq(JOptionPane.INFORMATION_MESSAGE)
            ));
        }
    }

    @Test
    void deleteSelectedItem_cancelled_doesNothing() {
        when(mockItemsTable.getSelectedRow()).thenReturn(0);
        when(mockItemsTable.convertRowIndexToModel(0)).thenReturn(0);
        Flower flowerToDelete = new Flower(FlowerType.DAISY, 2, 70, 20, "White", "UA", false, "d.jpg", 50);
        flowerToDelete.setId(1);
        when(mockFlowerDAO.getFlowerById(1)).thenReturn(flowerToDelete);
        lenient().when(mockTableModel.getRowCount()).thenReturn(1);
        lenient().when(mockTableModel.getValueAt(0, 0)).thenReturn(1);

        try (MockedStatic<JOptionPane> mockedOptionPane = Mockito.mockStatic(JOptionPane.class)) {
            mockedOptionPane.when(() -> JOptionPane.showConfirmDialog(any(), anyString(), anyString(), eq(JOptionPane.YES_NO_OPTION), eq(JOptionPane.WARNING_MESSAGE)))
                    .thenReturn(JOptionPane.NO_OPTION);
            flowersTab.deleteSelectedItem(null);
            verify(mockFlowerDAO, never()).deleteFlower(anyInt());
        }
    }

    @Test
    void deleteSelectedItem_itemToDeleteIsNull_logsErrorAndDoesNotProceed() {
        when(mockItemsTable.getSelectedRow()).thenReturn(0);
        when(mockItemsTable.convertRowIndexToModel(0)).thenReturn(0);
        when(mockFlowerDAO.getFlowerById(anyInt())).thenReturn(null);
        lenient().when(mockTableModel.getRowCount()).thenReturn(1);
        lenient().when(mockTableModel.getValueAt(0, 0)).thenReturn(1);

        try (MockedStatic<JOptionPane> mockedOptionPane = Mockito.mockStatic(JOptionPane.class)) {
            flowersTab.deleteSelectedItem(null);
            mockedOptionPane.verifyNoInteractions();
            verify(mockFlowerDAO, never()).deleteFlower(anyInt());
        }
    }

    @Test
    void getDetailsPanelTitle_returnsCorrectTitle() {
        assertEquals("Деталі квітки", flowersTab.getDetailsPanelTitle());
    }

    @Test
    void getDetailedInfoForItem_withFlower_returnsHtmlFromFlower() {
        Flower mockFlower = mock(Flower.class);
        String expectedHtml = "<html><body>Деталі про квітку</body></html>";
        when(mockFlower.getDetailedInfo()).thenReturn(expectedHtml);
        assertEquals(expectedHtml, flowersTab.getDetailedInfoForItem(mockFlower));
        verify(mockFlower).getDetailedInfo();
    }

    @Test
    void getDetailedInfoForItem_withNullFlower_returnsEmptyString() {
        assertEquals("", flowersTab.getDetailedInfoForItem(null));
    }

    @Test
    void getImagePathForItem_withFlower_returnsPathFromFlower() {
        Flower mockFlower = mock(Flower.class);
        String expectedPath = "path/image.jpg";
        when(mockFlower.getImagePath()).thenReturn(expectedPath);
        assertEquals(expectedPath, flowersTab.getImagePathForItem(mockFlower));
        verify(mockFlower).getImagePath();
    }

    @Test
    void getImagePathForItem_withNullFlower_returnsNull() {
        assertNull(flowersTab.getImagePathForItem(null));
    }

    @Test
    void hasStockLevelBar_returnsTrue() {
        assertTrue(flowersTab.hasStockLevelBar());
    }

    @Test
    void updateStockLevelBar_withFlower_setsValuesAndColors() {
        Flower flower = new Flower(FlowerType.ROSE, 10, 90, 50, "R", "NL", false, "", 10);
        flowersTab.updateStockLevelBar(flower);
        verify(mockStockLevelBar).setMaximum(200);
        verify(mockStockLevelBar).setValue(10);
        verify(mockStockLevelBar).setString("10 шт.");
        verify(mockStockLevelBar).setForeground(new Color(255, 69, 0));

        reset(mockStockLevelBar);
        flower.setStockQuantity(30);
        flowersTab.updateStockLevelBar(flower);
        verify(mockStockLevelBar).setValue(30);
        verify(mockStockLevelBar).setString("30 шт.");
        verify(mockStockLevelBar).setForeground(new Color(255, 165, 0));

        reset(mockStockLevelBar);
        flower.setStockQuantity(100);
        flowersTab.updateStockLevelBar(flower);
        verify(mockStockLevelBar).setValue(100);
        verify(mockStockLevelBar).setString("100 шт.");
        verify(mockStockLevelBar).setForeground(AbstractItemTab.PRIMARY_COLOR);

        reset(mockStockLevelBar);
        flower.setStockQuantity(250);
        flowersTab.updateStockLevelBar(flower);
        verify(mockStockLevelBar).setValue(200);
        verify(mockStockLevelBar).setString("250 шт.");
        verify(mockStockLevelBar).setForeground(AbstractItemTab.PRIMARY_COLOR);
    }

    @Test
    void updateStockLevelBar_withNullFlowerOrBar_doesNothing() {
        flowersTab.updateStockLevelBar(null);
        verify(mockStockLevelBar, never()).setValue(anyInt());

        replaceFieldWithReflection(flowersTab, "stockLevelBar", null, AbstractItemTab.class);
        Flower flower = new Flower(FlowerType.ROSE, 10, 90, 50, "R", "NL", false, "", 10);
        assertDoesNotThrow(() -> flowersTab.updateStockLevelBar(flower));
    }

    @Test
    void getItemNameSingular_returnsCorrectName() {
        assertEquals("квітка", flowersTab.getItemNameSingular());
    }

}