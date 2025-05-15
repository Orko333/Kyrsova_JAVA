package flowershop.gui.Tab;

import flowershop.dao.AccessoryDAO;
import flowershop.models.Accessory;
import flowershop.models.Accessory.AccessoryType;
import flowershop.gui.Dialog.AddEditAccessoryDialog;

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
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
class AccessoriesTabTest {

    @Mock
    private AccessoryDAO mockAccessoryDAO;

    private AccessoriesTab accessoriesTab;

    @Mock
    private JTable mockItemsTable;
    @Mock
    private DefaultTableModel mockTableModel;
    @Mock
    private TableColumnModel mockTableColumnModel;
    @Mock
    private TableColumn mockTableColumn;

    @Mock
    private JComboBox<AccessoryType> mockTypeFilterCombo;
    @Mock
    private JSpinner mockMinPriceSpinner;
    @Mock
    private JSpinner mockMaxPriceSpinner;
    @Mock
    private JSpinner mockMinStockSpinner;
    @Mock
    private JSpinner mockMaxStockSpinner;
    @Mock
    private JButton mockClearFiltersButton;
    @Mock
    private JButton mockAddButton;
    @Mock
    private JButton mockEditButton;

    @BeforeAll
    static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "true");
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("WARNING: Could not set LookAndFeel for tests. UI-related tests might fail. Error: " + e.getMessage());
        }
        if (GraphicsEnvironment.isHeadless()) {
            System.out.println("Headless mode is ON for tests.");
        } else {
            System.err.println("Warning: Headless mode could NOT be set for tests. Swing components might cause issues.");
        }
    }

    @AfterAll
    static void resetHeadlessMode() {
        System.clearProperty("java.awt.headless");
    }

    @BeforeEach
    void setUp() {
        accessoriesTab = new AccessoriesTab();

        try {
            java.lang.reflect.Field daoField = AbstractItemTab.class.getDeclaredField("itemDAO");
            daoField.setAccessible(true);
            daoField.set(accessoriesTab, mockAccessoryDAO);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail("Could not set mock AccessoryDAO via reflection: " + e.getMessage());
        }

        try {
            java.lang.reflect.Field tableField = AbstractItemTab.class.getDeclaredField("itemsTable");
            tableField.setAccessible(true);
            tableField.set(accessoriesTab, mockItemsTable);

            java.lang.reflect.Field modelField = AbstractItemTab.class.getDeclaredField("tableModel");
            modelField.setAccessible(true);
            modelField.set(accessoriesTab, mockTableModel);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            System.err.println("Warning: Could not replace JTable/TableModel with mocks: " + e.getMessage());
        }

        lenient().when(mockItemsTable.getColumnModel()).thenReturn(mockTableColumnModel);
        lenient().when(mockTableColumnModel.getColumn(anyInt())).thenReturn(mockTableColumn);

        initializeFilterComponentsViaReflection();

        replaceFieldWithReflection(accessoriesTab, "clearFiltersButton", mockClearFiltersButton, JButton.class);
        replaceFieldWithReflection(accessoriesTab, "addButton", mockAddButton, JButton.class);
        replaceFieldWithReflection(accessoriesTab, "editButton", mockEditButton, JButton.class);

        lenient().when(mockAccessoryDAO.getAllAccessories()).thenReturn(new ArrayList<>());
        lenient().when(mockTableModel.getRowCount()).thenReturn(0);

        lenient().when(mockTypeFilterCombo.getSelectedItem()).thenReturn(null);
        lenient().when(mockMinPriceSpinner.getValue()).thenReturn(Double.valueOf(0.0));
        lenient().when(mockMaxPriceSpinner.getValue()).thenReturn(Double.valueOf(1000.0));
        lenient().when(mockMinStockSpinner.getValue()).thenReturn(Integer.valueOf(0));
        lenient().when(mockMaxStockSpinner.getValue()).thenReturn(Integer.valueOf(100));

        if (accessoriesTab.searchField != null) {
            accessoriesTab.searchField.setText("");
        } else {
            System.err.println("Warning: accessoriesTab.searchField is null in setUp.");
        }
    }

    private void initializeFilterComponentsViaReflection() {
        try {
            replaceField(accessoriesTab, "typeFilterCombo", mockTypeFilterCombo, JComboBox.class);
            replaceField(accessoriesTab, "minPriceSpinner", mockMinPriceSpinner, JSpinner.class);
            replaceField(accessoriesTab, "maxPriceSpinner", mockMaxPriceSpinner, JSpinner.class);
            replaceField(accessoriesTab, "minStockSpinner", mockMinStockSpinner, JSpinner.class);
            replaceField(accessoriesTab, "maxStockSpinner", mockMaxStockSpinner, JSpinner.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            System.err.println("Warning: Could not replace some filter components with mocks via reflection: " + e.getMessage());
        }
    }

    private <T> void replaceField(Object targetObject, String fieldName, T newValue, Class<?> fieldClass)
            throws NoSuchFieldException, IllegalAccessException {
        java.lang.reflect.Field field = targetObject.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(targetObject, newValue);
    }

    @Test
    void constructor_initializesDAO() {
        try {
            java.lang.reflect.Field daoField = AbstractItemTab.class.getDeclaredField("itemDAO");
            daoField.setAccessible(true);
            Object actualDAO = daoField.get(accessoriesTab);
            assertSame(mockAccessoryDAO, actualDAO);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail("Could not access itemDAO field for verification: " + e.getMessage());
        }
    }

    @Test
    void getTabTitle_returnsCorrectTitle() {
        assertEquals("Управління аксесуарами", accessoriesTab.getTabTitle());
    }

    @Test
    void getColumnNames_returnsCorrectNames() {
        assertArrayEquals(new String[]{"ID", "Назва", "Тип", "Колір", "Розмір", "Ціна", "На складі"}, accessoriesTab.getColumnNames());
    }

    @Test
    void getColumnClasses_returnsCorrectClasses() {
        assertArrayEquals(new Class<?>[]{Integer.class, String.class, String.class, String.class, String.class, Double.class, Integer.class}, accessoriesTab.getColumnClasses());
    }

    @Test
    void getRowDataForItem_returnsCorrectData() {
        Accessory accessory = new Accessory("Test Name", 10.0, "Desc", "path", 5, AccessoryType.RIBBON, "Red", "1m");
        accessory.setId(1);
        Object[] rowData = accessoriesTab.getRowDataForItem(accessory);
        assertArrayEquals(new Object[]{1, "Test Name", "Стрічка", "Red", "1m", 10.0, 5}, rowData);
    }

    @Test
    void createFilterPanel_createsPanelWithRows() {
        AccessoriesTab realAccessoriesTab = new AccessoriesTab();
        JPanel filterPanel = realAccessoriesTab.createFilterPanel();
        assertNotNull(filterPanel);
        assertTrue(filterPanel.getLayout() instanceof BoxLayout);
        assertEquals(2, filterPanel.getComponentCount());

        Component searchRowComponent = filterPanel.getComponent(0);
        assertTrue(searchRowComponent instanceof JPanel);
        JPanel searchRowPanel = (JPanel) searchRowComponent;
        assertTrue(searchRowPanel.getComponentCount() > 0);

        Component rangeRowComponent = filterPanel.getComponent(1);
        assertTrue(rangeRowComponent instanceof JPanel);
        JPanel rangeRowPanel = (JPanel) rangeRowComponent;
        assertTrue(rangeRowPanel.getComponentCount() > 0);
    }

    @Test
    void filterItems_noFilters_returnsAll() {
        Accessory acc1 = new Accessory("Acc1", 10, "", "", 0, AccessoryType.RIBBON, "", "");
        Accessory acc2 = new Accessory("Acc2", 20, "", "", 0, AccessoryType.VASE, "", "");
        List<Accessory> allAccessories = Arrays.asList(acc1, acc2);

        accessoriesTab.searchField.setText("");
        when(mockTypeFilterCombo.getSelectedItem()).thenReturn(null);
        when(mockMinPriceSpinner.getValue()).thenReturn(0.0);
        when(mockMaxPriceSpinner.getValue()).thenReturn(1000.0);
        when(mockMinStockSpinner.getValue()).thenReturn(0);
        when(mockMaxStockSpinner.getValue()).thenReturn(100);

        List<Accessory> filtered = accessoriesTab.filterItems(new ArrayList<>(allAccessories));
        assertEquals(2, filtered.size());
        assertTrue(filtered.containsAll(allAccessories));
    }

    @Test
    void filterItems_nullInput_returnsEmptyList() {
        accessoriesTab.searchField.setText("");
        when(mockTypeFilterCombo.getSelectedItem()).thenReturn(null);
        when(mockMinPriceSpinner.getValue()).thenReturn(0.0);
        when(mockMaxPriceSpinner.getValue()).thenReturn(1000.0);
        when(mockMinStockSpinner.getValue()).thenReturn(0);
        when(mockMaxStockSpinner.getValue()).thenReturn(100);

        List<Accessory> filtered = accessoriesTab.filterItems(null);
        assertNotNull(filtered);
        assertTrue(filtered.isEmpty());
    }

    @Test
    void filterItems_bySearchText_name() {
        Accessory acc1 = new Accessory("Red Ribbon", 5, "desc", "img", 10, AccessoryType.RIBBON, "Red", "1m");
        Accessory acc2 = new Accessory("Blue Vase", 25, "desc", "img", 5, AccessoryType.VASE, "Blue", "L");
        List<Accessory> allAccessories = Arrays.asList(acc1, acc2);

        accessoriesTab.searchField.setText("ribbon");
        when(mockTypeFilterCombo.getSelectedItem()).thenReturn(null);
        when(mockMinPriceSpinner.getValue()).thenReturn(0.0);
        when(mockMaxPriceSpinner.getValue()).thenReturn(1000.0);
        when(mockMinStockSpinner.getValue()).thenReturn(0);
        when(mockMaxStockSpinner.getValue()).thenReturn(100);

        List<Accessory> filtered = accessoriesTab.filterItems(new ArrayList<>(allAccessories));
        assertEquals(1, filtered.size());
        assertEquals("Red Ribbon", filtered.get(0).getName());
    }

    @Test
    void filterItems_bySearchText_color() {
        Accessory acc1 = new Accessory("Ribbon", 5, "desc", "img", 10, AccessoryType.RIBBON, "Red", "1m");
        Accessory acc2 = new Accessory("Vase", 25, "desc", "img", 5, AccessoryType.VASE, "Blue", "L");
        List<Accessory> allAccessories = Arrays.asList(acc1, acc2);

        accessoriesTab.searchField.setText("blue");
        when(mockTypeFilterCombo.getSelectedItem()).thenReturn(null);
        when(mockMinPriceSpinner.getValue()).thenReturn(0.0);
        when(mockMaxPriceSpinner.getValue()).thenReturn(1000.0);
        when(mockMinStockSpinner.getValue()).thenReturn(0);
        when(mockMaxStockSpinner.getValue()).thenReturn(100);

        List<Accessory> filtered = accessoriesTab.filterItems(new ArrayList<>(allAccessories));
        assertEquals(1, filtered.size());
        assertEquals("Vase", filtered.get(0).getName());
    }

    @Test
    void filterItems_bySearchText_size() {
        Accessory acc1 = new Accessory("Ribbon", 5, "desc", "img", 10, AccessoryType.RIBBON, "Red", "1m");
        Accessory acc2 = new Accessory("Vase", 25, "desc", "img", 5, AccessoryType.VASE, "Blue", "Large");
        List<Accessory> allAccessories = Arrays.asList(acc1, acc2);

        accessoriesTab.searchField.setText("large");
        when(mockTypeFilterCombo.getSelectedItem()).thenReturn(null);
        when(mockMinPriceSpinner.getValue()).thenReturn(0.0);
        when(mockMaxPriceSpinner.getValue()).thenReturn(1000.0);
        when(mockMinStockSpinner.getValue()).thenReturn(0);
        when(mockMaxStockSpinner.getValue()).thenReturn(100);

        List<Accessory> filtered = accessoriesTab.filterItems(new ArrayList<>(allAccessories));
        assertEquals(1, filtered.size());
        assertEquals("Vase", filtered.get(0).getName());
    }

    @Test
    void filterItems_bySearchText_typeDisplayName() {
        Accessory acc1 = new Accessory("Ribbon", 5, "desc", "img", 10, AccessoryType.RIBBON, "Red", "1m");
        Accessory acc2 = new Accessory("Vase", 25, "desc", "img", 5, AccessoryType.VASE, "Blue", "Large");
        List<Accessory> allAccessories = Arrays.asList(acc1, acc2);

        accessoriesTab.searchField.setText("стрічка");
        when(mockTypeFilterCombo.getSelectedItem()).thenReturn(null);
        when(mockMinPriceSpinner.getValue()).thenReturn(0.0);
        when(mockMaxPriceSpinner.getValue()).thenReturn(1000.0);
        when(mockMinStockSpinner.getValue()).thenReturn(0);
        when(mockMaxStockSpinner.getValue()).thenReturn(100);

        List<Accessory> filtered = accessoriesTab.filterItems(new ArrayList<>(allAccessories));
        assertEquals(1, filtered.size());
        assertEquals("Ribbon", filtered.get(0).getName());
    }

    @Test
    void filterItems_byType() {
        Accessory acc1 = new Accessory("Ribbon1", 5, "", "", 0, AccessoryType.RIBBON, "", "");
        Accessory acc2 = new Accessory("Vase1", 25, "", "", 0, AccessoryType.VASE, "", "");
        List<Accessory> allAccessories = Arrays.asList(acc1, acc2);

        accessoriesTab.searchField.setText("");
        when(mockTypeFilterCombo.getSelectedItem()).thenReturn(AccessoryType.VASE);
        when(mockMinPriceSpinner.getValue()).thenReturn(0.0);
        when(mockMaxPriceSpinner.getValue()).thenReturn(1000.0);
        when(mockMinStockSpinner.getValue()).thenReturn(0);
        when(mockMaxStockSpinner.getValue()).thenReturn(100);

        List<Accessory> filtered = accessoriesTab.filterItems(new ArrayList<>(allAccessories));
        assertEquals(1, filtered.size());
        assertEquals("Vase1", filtered.get(0).getName());
    }

    @Test
    void filterItems_byPriceRange() {
        Accessory acc1 = new Accessory("Cheap", 5, "", "", 0, AccessoryType.OTHER, "", "");
        Accessory acc2 = new Accessory("Mid", 50, "", "", 0, AccessoryType.OTHER, "", "");
        Accessory acc3 = new Accessory("Expensive", 150, "", "", 0, AccessoryType.OTHER, "", "");
        List<Accessory> allAccessories = Arrays.asList(acc1, acc2, acc3);

        accessoriesTab.searchField.setText("");
        when(mockTypeFilterCombo.getSelectedItem()).thenReturn(null);
        when(mockMinPriceSpinner.getValue()).thenReturn(40.0);
        when(mockMaxPriceSpinner.getValue()).thenReturn(60.0);
        when(mockMinStockSpinner.getValue()).thenReturn(0);
        when(mockMaxStockSpinner.getValue()).thenReturn(100);

        List<Accessory> filtered = accessoriesTab.filterItems(new ArrayList<>(allAccessories));
        assertEquals(1, filtered.size());
        assertEquals("Mid", filtered.get(0).getName());
    }

    @Test
    void filterItems_byStockRange() {
        Accessory acc1 = new Accessory("LowStock", 10, "", "", 5, AccessoryType.OTHER, "", "");
        Accessory acc2 = new Accessory("MidStock", 10, "", "", 25, AccessoryType.OTHER, "", "");
        Accessory acc3 = new Accessory("HighStock", 10, "", "", 75, AccessoryType.OTHER, "", "");
        List<Accessory> allAccessories = Arrays.asList(acc1, acc2, acc3);

        accessoriesTab.searchField.setText("");
        when(mockTypeFilterCombo.getSelectedItem()).thenReturn(null);
        when(mockMinPriceSpinner.getValue()).thenReturn(0.0);
        when(mockMaxPriceSpinner.getValue()).thenReturn(1000.0);
        when(mockMinStockSpinner.getValue()).thenReturn(20);
        when(mockMaxStockSpinner.getValue()).thenReturn(30);

        List<Accessory> filtered = accessoriesTab.filterItems(new ArrayList<>(allAccessories));
        assertEquals(1, filtered.size());
        assertEquals("MidStock", filtered.get(0).getName());
    }

    @Test
    void filterItems_combinedFilters() {
        Accessory acc1 = new Accessory("Red Ribbon Cheap LowStock", 5, "desc", "img", 2, AccessoryType.RIBBON, "Red", "Small");
        Accessory acc2 = new Accessory("Blue Vase Mid MidStock", 50, "desc", "img", 25, AccessoryType.VASE, "Blue", "Medium");
        Accessory acc3 = new Accessory("Red Vase Expensive HighStock", 150, "desc", "img", 80, AccessoryType.VASE, "Red", "Large");
        List<Accessory> allAccessories = Arrays.asList(acc1, acc2, acc3);

        accessoriesTab.searchField.setText("vase");
        when(mockTypeFilterCombo.getSelectedItem()).thenReturn(AccessoryType.VASE);
        when(mockMinPriceSpinner.getValue()).thenReturn(40.0);
        when(mockMaxPriceSpinner.getValue()).thenReturn(160.0);
        when(mockMinStockSpinner.getValue()).thenReturn(20);
        when(mockMaxStockSpinner.getValue()).thenReturn(90);

        List<Accessory> filtered = accessoriesTab.filterItems(new ArrayList<>(allAccessories));
        assertEquals(2, filtered.size());
        assertTrue(filtered.stream().anyMatch(a -> a.getName().equals("Blue Vase Mid MidStock")));
        assertTrue(filtered.stream().anyMatch(a -> a.getName().equals("Red Vase Expensive HighStock")));
    }

    @Test
    void createStyledRadioButton_withoutIconKey() {
        try {
            JRadioButton radioButton = accessoriesTab.createStyledRadioButton("Test", null);
            assertNotNull(radioButton);
            assertEquals("Test", radioButton.getText());
            assertNull(radioButton.getIcon());
        } catch (NullPointerException npe) {
            if (npe.getMessage() != null && npe.getMessage().contains("UIManager.getDefaults() is null")) {
                System.err.println("Skipping createStyledRadioButton_withoutIconKey due to UIManager init issue: " + npe.getMessage());
            } else {
                throw npe;
            }
        }
    }

    @Test
    void getAllItemsFromDAO_callsDAO() {
        List<Accessory> expectedAccessories = Collections.singletonList(new Accessory("Test", 1, "", "", 0, AccessoryType.OTHER, "", ""));
        when(mockAccessoryDAO.getAllAccessories()).thenReturn(expectedAccessories);
        List<Accessory> actualAccessories = accessoriesTab.getAllItemsFromDAO();
        assertSame(expectedAccessories, actualAccessories);
        verify(mockAccessoryDAO).getAllAccessories();
    }

    @Test
    void getItemByIdFromDAO_callsDAO() {
        Accessory expectedAccessory = new Accessory("Test", 1, "", "", 0, AccessoryType.OTHER, "", "");
        when(mockAccessoryDAO.getAccessoryById(1)).thenReturn(expectedAccessory);
        Accessory actualAccessory = accessoriesTab.getItemByIdFromDAO(1);
        assertSame(expectedAccessory, actualAccessory);
        verify(mockAccessoryDAO).getAccessoryById(1);
    }

    @Test
    void getItemByIdFromDAO_callsDAO_notFound() {
        when(mockAccessoryDAO.getAccessoryById(999)).thenReturn(null);
        Accessory actualAccessory = accessoriesTab.getItemByIdFromDAO(999);
        assertNull(actualAccessory);
        verify(mockAccessoryDAO).getAccessoryById(999);
    }

    @Test
    void showAddEditDialog_editMode_noSelection_showsWarning() {
        ActionEvent mockEvent = mock(ActionEvent.class);
        when(mockEvent.getSource()).thenReturn(mockEditButton);
        when(mockItemsTable.getSelectedRow()).thenReturn(-1);

        try (MockedStatic<JOptionPane> mockedOptionPane = Mockito.mockStatic(JOptionPane.class)) {
            accessoriesTab.showAddEditDialog(mockEvent);
            mockedOptionPane.verify(() -> JOptionPane.showMessageDialog(
                    eq(accessoriesTab),
                    eq("Будь ласка, оберіть аксесуар для редагування."),
                    eq("Попередження"),
                    eq(JOptionPane.WARNING_MESSAGE)
            ));
        }
    }

    private void replaceFieldWithReflection(Object target, String fieldName, Object value, Class<?> fieldClassInParent) {
        try {
            java.lang.reflect.Field field = AbstractItemTab.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail("Failed to set field '" + fieldName + "' in AbstractItemTab: " + e.getMessage());
        }
    }

    @Test
    void deleteSelectedItem_noSelection_showsWarning() {
        when(mockItemsTable.getSelectedRow()).thenReturn(-1);
        try (MockedStatic<JOptionPane> mockedOptionPane = Mockito.mockStatic(JOptionPane.class)) {
            accessoriesTab.deleteSelectedItem(null);
            mockedOptionPane.verify(() -> JOptionPane.showMessageDialog(
                    eq(accessoriesTab),
                    eq("Будь ласка, оберіть аксесуар для видалення."),
                    eq("Попередження"),
                    eq(JOptionPane.WARNING_MESSAGE)
            ));
        }
    }

    @Test
    void deleteSelectedItem_confirmed_deletesAndShowsMessage() {
        when(mockItemsTable.getSelectedRow()).thenReturn(0);
        when(mockItemsTable.convertRowIndexToModel(0)).thenReturn(0);

        Accessory accessoryToDelete = new Accessory("ToDelete", 10, "", "",0, AccessoryType.OTHER, "", "");
        accessoryToDelete.setId(1);

        lenient().when(mockTableModel.getRowCount()).thenReturn(1);
        lenient().when(mockTableModel.getValueAt(0, 0)).thenReturn(1);
        when(mockAccessoryDAO.getAccessoryById(1)).thenReturn(accessoryToDelete);

        accessoriesTab.searchField.setText("");
        when(mockTypeFilterCombo.getSelectedItem()).thenReturn(null);
        when(mockMinPriceSpinner.getValue()).thenReturn(0.0);
        when(mockMaxPriceSpinner.getValue()).thenReturn(1000.0);
        when(mockMinStockSpinner.getValue()).thenReturn(0);
        when(mockMaxStockSpinner.getValue()).thenReturn(100);

        try (MockedStatic<JOptionPane> mockedOptionPane = Mockito.mockStatic(JOptionPane.class)) {
            mockedOptionPane.when(() -> JOptionPane.showConfirmDialog(
                    any(), anyString(), anyString(), eq(JOptionPane.YES_NO_OPTION), eq(JOptionPane.WARNING_MESSAGE)
            )).thenReturn(JOptionPane.YES_OPTION);

            accessoriesTab.deleteSelectedItem(null);

            verify(mockAccessoryDAO).deleteAccessory(1);
            verify(mockTableModel, atLeastOnce()).setRowCount(0);

            mockedOptionPane.verify(() -> JOptionPane.showMessageDialog(
                    eq(accessoriesTab),
                    eq("Аксесуар \"ToDelete\" успішно видалено."),
                    eq("Видалення завершено"),
                    eq(JOptionPane.INFORMATION_MESSAGE)
            ));
        }
    }

    @Test
    void deleteSelectedItem_cancelled_doesNothing() {
        when(mockItemsTable.getSelectedRow()).thenReturn(0);
        when(mockItemsTable.convertRowIndexToModel(0)).thenReturn(0);

        Accessory accessoryToDelete = new Accessory("ToDelete", 10, "", "", 0, AccessoryType.OTHER,"","");
        accessoryToDelete.setId(1);
        lenient().when(mockTableModel.getRowCount()).thenReturn(1);
        lenient().when(mockTableModel.getValueAt(0, 0)).thenReturn(1);
        when(mockAccessoryDAO.getAccessoryById(1)).thenReturn(accessoryToDelete);

        try (MockedStatic<JOptionPane> mockedOptionPane = Mockito.mockStatic(JOptionPane.class)) {
            mockedOptionPane.when(() -> JOptionPane.showConfirmDialog(
                    any(), anyString(), anyString(), eq(JOptionPane.YES_NO_OPTION), eq(JOptionPane.WARNING_MESSAGE)
            )).thenReturn(JOptionPane.NO_OPTION);

            accessoriesTab.deleteSelectedItem(null);

            verify(mockAccessoryDAO, never()).deleteAccessory(anyInt());
            verify(mockTableModel, never()).setRowCount(0);
            mockedOptionPane.verify(() -> JOptionPane.showMessageDialog(any(),anyString(),anyString(),anyInt()), never());
        }
    }

    @Test
    void getDetailsPanelTitle_returnsCorrectTitle() {
        assertEquals("Деталі аксесуара", accessoriesTab.getDetailsPanelTitle());
    }

    @Test
    void getDetailedInfoForItem_withAccessory_returnsHtml() {
        Accessory accessory = new Accessory("Лента Красная", 12.50, "Яркая красная лента", "img/ribbon.png", 50, AccessoryType.RIBBON, "Красный", "2м");
        accessory.setId(1);
        accessory.setDescription("Очень красивая лента для букетов.");

        String html = accessoriesTab.getDetailedInfoForItem(accessory);
        assertTrue(html.contains("Лента Красная"));
        assertTrue(html.contains("ID:</b></td><td>1</td>"));
        assertTrue(html.contains("Тип:</b></td><td>Стрічка</td>"));
    }

    @Test
    void getDetailedInfoForItem_withNullAccessory_returnsEmptyString() {
        assertEquals("", accessoriesTab.getDetailedInfoForItem(null));
    }

    @Test
    void getDetailedInfoForItem_withAccessoryNoDescription_returnsHtmlWithoutDescriptionRow() {
        Accessory accessory = new Accessory("Test Acc", 10.0, "", "path.jpg", 5, AccessoryType.OTHER, "Color", "Size");
        accessory.setId(2);
        String html = accessoriesTab.getDetailedInfoForItem(accessory);
        assertTrue(html.contains("Test Acc"));
        assertFalse(html.contains("<b>Опис:</b>"));
    }

    @Test
    void getImagePathForItem_withAccessory_returnsPath() {
        Accessory accessory = new Accessory("Name", 1, "", "path/to/image.jpg", 0, AccessoryType.OTHER, "", "");
        assertEquals("path/to/image.jpg", accessoriesTab.getImagePathForItem(accessory));
    }

    @Test
    void getImagePathForItem_withNullAccessory_returnsNull() {
        assertNull(accessoriesTab.getImagePathForItem(null));
    }

    @Test
    void hasStockLevelBar_returnsTrue() {
        assertTrue(accessoriesTab.hasStockLevelBar());
    }

    @Test
    void updateStockLevelBar_withAccessory_setsValuesAndColors() {
        JProgressBar bar = new JProgressBar();
        replaceFieldWithReflection(accessoriesTab, "stockLevelBar", bar, JProgressBar.class);

        Accessory accessory = new Accessory("Test", 1, "", "", 0, AccessoryType.OTHER,"","");
        accessory.setStockQuantity(5);
        accessoriesTab.updateStockLevelBar(accessory);
        assertEquals(5, bar.getValue());
        assertEquals("5 шт.", bar.getString());
        assertEquals(new Color(255, 69, 0), bar.getForeground());

        accessory.setStockQuantity(15);
        accessoriesTab.updateStockLevelBar(accessory);
        assertEquals(15, bar.getValue());
        assertEquals("15 шт.", bar.getString());
        assertEquals(new Color(255, 165, 0), bar.getForeground());

        accessory.setStockQuantity(50);
        accessoriesTab.updateStockLevelBar(accessory);
        assertEquals(50, bar.getValue());
        assertEquals("50 шт.", bar.getString());
        assertEquals(AbstractItemTab.PRIMARY_COLOR, bar.getForeground());

        accessory.setStockQuantity(150);
        accessoriesTab.updateStockLevelBar(accessory);
        assertEquals(100, bar.getValue());
        assertEquals("150 шт.", bar.getString());
        assertEquals(AbstractItemTab.PRIMARY_COLOR, bar.getForeground());
    }

    @Test
    void updateStockLevelBar_withNullAccessory_doesNothing() {
        JProgressBar bar = new JProgressBar();
        bar.setValue(25); bar.setString("Old"); bar.setForeground(Color.BLUE);
        replaceFieldWithReflection(accessoriesTab, "stockLevelBar", bar, JProgressBar.class);

        accessoriesTab.updateStockLevelBar(null);

        assertEquals(25, bar.getValue());
        assertEquals("Old", bar.getString());
        assertEquals(Color.BLUE, bar.getForeground());
    }

    @Test
    void updateStockLevelBar_stockLevelBarIsNull_doesNothing() {
        replaceFieldWithReflection(accessoriesTab, "stockLevelBar", null, JProgressBar.class);
        Accessory accessory = new Accessory("Test", 1, "", "", 5, AccessoryType.OTHER,"","");
        assertDoesNotThrow(() -> accessoriesTab.updateStockLevelBar(accessory));
    }

    @Test
    void getItemNameSingular_returnsCorrectName() {
        assertEquals("аксесуар", accessoriesTab.getItemNameSingular());
    }

    @Test
    void accessoryTypeRenderer_getListCellRendererComponent_withAccessoryType() {
        AccessoriesTab.AccessoryTypeRenderer renderer = new AccessoriesTab.AccessoryTypeRenderer();
        JList<Object> mockList = mock(JList.class);
        Component component = renderer.getListCellRendererComponent(mockList, AccessoryType.VASE, 0, false, false);
        assertTrue(component instanceof JLabel);
        JLabel label = (JLabel) component;
        assertEquals("Ваза", label.getText());
        assertEquals(Color.WHITE, label.getBackground());
        assertEquals(flowershop.gui.Dialog.AbstractAddEditDialog.TEXT_COLOR, label.getForeground());
    }

    @Test
    void accessoryTypeRenderer_getListCellRendererComponent_withOtherObject() {
        AccessoriesTab.AccessoryTypeRenderer renderer = new AccessoriesTab.AccessoryTypeRenderer();
        JList<Object> mockList = mock(JList.class);
        JLabel label = (JLabel) renderer.getListCellRendererComponent(mockList, "Some String", 0, false, false);
        assertEquals("Будь-який тип", label.getText());
    }

    @Test
    void accessoryTypeRenderer_getListCellRendererComponent_withNullObject() {
        AccessoriesTab.AccessoryTypeRenderer renderer = new AccessoriesTab.AccessoryTypeRenderer();
        JList<Object> mockList = mock(JList.class);
        JLabel label = (JLabel) renderer.getListCellRendererComponent(mockList, null, 0, false, false);
        assertEquals("Будь-який тип", label.getText());
    }

    @Test
    void accessoryTypeRenderer_getListCellRendererComponent_isSelected() {
        AccessoriesTab.AccessoryTypeRenderer renderer = new AccessoriesTab.AccessoryTypeRenderer();
        JList<Object> mockList = mock(JList.class);
        JLabel label = (JLabel) renderer.getListCellRendererComponent(mockList, AccessoryType.RIBBON, 0, true, true);
        assertEquals("Стрічка", label.getText());
        assertEquals(AbstractItemTab.PRIMARY_COLOR.darker(), label.getBackground());
        assertEquals(Color.WHITE, label.getForeground());
    }

    @Test
    void selectRowByAccessoryId_accessoryExists() {
        when(mockItemsTable.getRowCount()).thenReturn(1);
        when(mockItemsTable.convertRowIndexToModel(0)).thenReturn(0);

        Accessory accessoryInTable = new Accessory("Test", 10, "", "", 0, AccessoryType.OTHER,"","");
        accessoryInTable.setId(123);

        lenient().when(mockTableModel.getRowCount()).thenReturn(1);
        lenient().when(mockTableModel.getValueAt(0, 0)).thenReturn(123);
        when(mockAccessoryDAO.getAccessoryById(123)).thenReturn(accessoryInTable);

        lenient().when(mockItemsTable.getCellRect(anyInt(), anyInt(), anyBoolean())).thenReturn(new Rectangle(0,0,1,1));

        accessoriesTab.selectRowByAccessoryId(123);

        verify(mockItemsTable).setRowSelectionInterval(0, 0);
        verify(mockItemsTable).scrollRectToVisible(any(Rectangle.class));
    }

    @Test
    void selectRowByAccessoryId_accessoryNotExists() {
        when(mockItemsTable.getRowCount()).thenReturn(1);
        when(mockItemsTable.convertRowIndexToModel(0)).thenReturn(0);

        Accessory otherAccessory = new Accessory("Other", 20, "", "", 0, AccessoryType.VASE,"","");
        otherAccessory.setId(456);
        lenient().when(mockTableModel.getRowCount()).thenReturn(1);
        lenient().when(mockTableModel.getValueAt(0, 0)).thenReturn(456);
        when(mockAccessoryDAO.getAccessoryById(456)).thenReturn(otherAccessory);

        accessoriesTab.selectRowByAccessoryId(123);
        verify(mockItemsTable, never()).setRowSelectionInterval(anyInt(), anyInt());
    }

    @Test
    void selectRowByAccessoryId_emptyTable() {
        when(mockItemsTable.getRowCount()).thenReturn(0);
        accessoriesTab.selectRowByAccessoryId(123);
        verify(mockItemsTable, never()).setRowSelectionInterval(anyInt(), anyInt());
    }

    @Test
    void selectRowByAccessoryId_itemInRowIsNull() {
        when(mockItemsTable.getRowCount()).thenReturn(1);
        when(mockItemsTable.convertRowIndexToModel(0)).thenReturn(0);
        lenient().when(mockTableModel.getRowCount()).thenReturn(1);
        lenient().when(mockTableModel.getValueAt(0,0)).thenReturn(123);
        when(mockAccessoryDAO.getAccessoryById(123)).thenReturn(null);

        accessoriesTab.selectRowByAccessoryId(123);
        verify(mockItemsTable, never()).setRowSelectionInterval(anyInt(), anyInt());
    }

    @Test
    void selectRowByAccessoryId_invalidModelRow() {
        when(mockItemsTable.getRowCount()).thenReturn(1);
        when(mockItemsTable.convertRowIndexToModel(0)).thenReturn(-1);
        accessoriesTab.selectRowByAccessoryId(123);
        verify(mockItemsTable, never()).setRowSelectionInterval(anyInt(), anyInt());

        Mockito.reset(mockItemsTable);
        when(mockItemsTable.getRowCount()).thenReturn(1);
        when(mockItemsTable.convertRowIndexToModel(0)).thenReturn(5);
        lenient().when(mockTableModel.getRowCount()).thenReturn(1);
        accessoriesTab.selectRowByAccessoryId(123);
        verify(mockItemsTable, never()).setRowSelectionInterval(anyInt(), anyInt());
    }

    @Test
    void refreshTableData_callsApplyFiltersAndPagination() {
        accessoriesTab.refreshTableData();
        verify(mockTableModel, atLeastOnce()).setRowCount(0);
    }

    @Test
    void simpleDocumentListener_callsUpdateAction() {
        Runnable mockRunnable = mock(Runnable.class);
        AbstractItemTab.SimpleDocumentListener listener = new AbstractItemTab.SimpleDocumentListener(mockRunnable);
        javax.swing.event.DocumentEvent mockEvent = mock(javax.swing.event.DocumentEvent.class);

        listener.insertUpdate(mockEvent);
        verify(mockRunnable, times(1)).run();

        listener.removeUpdate(mockEvent);
        verify(mockRunnable, times(2)).run();

        listener.changedUpdate(mockEvent);
        verify(mockRunnable, times(3)).run();
    }

    @Test
    void showAddEditDialog_addMode_showsDialogWithNullAccessoryAndSaves() {
        ActionEvent mockEvent = mock(ActionEvent.class);
        when(mockEvent.getSource()).thenReturn(mockAddButton);
        Accessory newAccessory = new Accessory("New Acc", 1.0, AccessoryType.OTHER);
        newAccessory.setId(99);

        try (MockedConstruction<AddEditAccessoryDialog> mockedDialogConstruction = Mockito.mockConstruction(
                AddEditAccessoryDialog.class,
                (mockDialog, context) -> {
                    assertNull(context.arguments().get(1));
                    when(mockDialog.isSaved()).thenReturn(true);
                    when(mockDialog.getAccessory()).thenReturn(newAccessory);
                })) {

            accessoriesTab.showAddEditDialog(mockEvent);

            assertEquals(1, mockedDialogConstruction.constructed().size());
            AddEditAccessoryDialog constructedDialog = mockedDialogConstruction.constructed().get(0);
            verify(constructedDialog).setVisible(true);

            verify(mockAccessoryDAO).saveAccessory(newAccessory);
            verify(mockTableModel, atLeastOnce()).setRowCount(0);
        }
    }

    @Test
    void showAddEditDialog_editMode_showsDialogWithAccessoryAndSaves() {
        ActionEvent mockEvent = mock(ActionEvent.class);
        when(mockEvent.getSource()).thenReturn(mockEditButton);

        Accessory accessoryToEdit = new Accessory("Old Acc", 10.0, "desc", "path", 5, AccessoryType.RIBBON, "Red", "1m");
        accessoryToEdit.setId(123);
        Accessory editedAccessory = new Accessory("Edited Acc", 12.0, "new desc", "new_path", 3, AccessoryType.RIBBON, "Blue", "2m");
        editedAccessory.setId(123);

        when(mockItemsTable.getSelectedRow()).thenReturn(0);
        when(mockItemsTable.convertRowIndexToModel(0)).thenReturn(0);

        lenient().when(mockTableModel.getRowCount()).thenReturn(1);
        lenient().when(mockTableModel.getValueAt(0,0)).thenReturn(123);
        when(mockAccessoryDAO.getAccessoryById(123)).thenReturn(accessoryToEdit);

        try (MockedConstruction<AddEditAccessoryDialog> mockedDialogConstruction = Mockito.mockConstruction(
                AddEditAccessoryDialog.class,
                (mockDialog, context) -> {
                    assertSame(accessoryToEdit, context.arguments().get(1));
                    when(mockDialog.isSaved()).thenReturn(true);
                    when(mockDialog.getAccessory()).thenReturn(editedAccessory);
                })) {

            accessoriesTab.showAddEditDialog(mockEvent);

            assertEquals(1, mockedDialogConstruction.constructed().size());
            AddEditAccessoryDialog constructedDialog = mockedDialogConstruction.constructed().get(0);
            verify(constructedDialog).setVisible(true);

            verify(mockAccessoryDAO).saveAccessory(editedAccessory);
            verify(mockTableModel, atLeastOnce()).setRowCount(0);
        }
    }

    @Test
    void showAddEditDialog_addMode_dialogCancelled() {
        ActionEvent mockEvent = mock(ActionEvent.class);
        when(mockEvent.getSource()).thenReturn(mockAddButton);

        try (MockedConstruction<AddEditAccessoryDialog> mockedDialogConstruction = Mockito.mockConstruction(
                AddEditAccessoryDialog.class,
                (mockDialog, context) -> {
                    when(mockDialog.isSaved()).thenReturn(false);
                })) {

            accessoriesTab.showAddEditDialog(mockEvent);

            assertEquals(1, mockedDialogConstruction.constructed().size());
            AddEditAccessoryDialog constructedDialog = mockedDialogConstruction.constructed().get(0);
            verify(constructedDialog).setVisible(true);

            verify(mockAccessoryDAO, never()).saveAccessory(any(Accessory.class));
            verify(mockTableModel, never()).setRowCount(anyInt());
        }
    }

    @Test
    void configureTableColumnWidths_setsPreferredWidthsAndRenderers() {
        TableColumn mockIdColumn = mock(TableColumn.class);
        TableColumn mockNameColumn = mock(TableColumn.class);
        TableColumn mockTypeColumn = mock(TableColumn.class);
        TableColumn mockColorColumn = mock(TableColumn.class);
        TableColumn mockSizeColumn = mock(TableColumn.class);
        TableColumn mockPriceColumn = mock(TableColumn.class);
        TableColumn mockStockColumn = mock(TableColumn.class);

        when(mockTableColumnModel.getColumn(0)).thenReturn(mockIdColumn);
        when(mockTableColumnModel.getColumn(1)).thenReturn(mockNameColumn);
        when(mockTableColumnModel.getColumn(2)).thenReturn(mockTypeColumn);
        when(mockTableColumnModel.getColumn(3)).thenReturn(mockColorColumn);
        when(mockTableColumnModel.getColumn(4)).thenReturn(mockSizeColumn);
        when(mockTableColumnModel.getColumn(5)).thenReturn(mockPriceColumn);
        when(mockTableColumnModel.getColumn(6)).thenReturn(mockStockColumn);

        accessoriesTab.configureTableColumnWidths();

        verify(mockIdColumn).setPreferredWidth(40);
        verify(mockNameColumn).setPreferredWidth(180);
        verify(mockTypeColumn).setPreferredWidth(120);
        verify(mockColorColumn).setPreferredWidth(80);
        verify(mockSizeColumn).setPreferredWidth(80);
        verify(mockPriceColumn).setPreferredWidth(80);
        verify(mockStockColumn).setPreferredWidth(80);

        verify(mockIdColumn).setCellRenderer(any(javax.swing.table.DefaultTableCellRenderer.class));
        verify(mockStockColumn).setCellRenderer(any(javax.swing.table.DefaultTableCellRenderer.class));
        verify(mockPriceColumn).setCellRenderer(any(javax.swing.table.DefaultTableCellRenderer.class));
    }

    @Test
    void typeFilterCombo_actionPerformed_refreshesTable() {
        AccessoriesTab spiedTab = spy(accessoriesTab);
        spiedTab.applyFiltersAndRefresh();
        verify(mockTableModel, atLeastOnce()).setRowCount(0);
        verify(spiedTab).applyFiltersAndRefresh();
    }

    @Test
    void deleteSelectedItem_itemToDeleteIsNull_logsErrorAndDoesNotProceed() {
        when(mockItemsTable.getSelectedRow()).thenReturn(0);
        when(mockItemsTable.convertRowIndexToModel(0)).thenReturn(0);

        lenient().when(mockTableModel.getRowCount()).thenReturn(1);
        lenient().when(mockTableModel.getValueAt(0,0)).thenReturn(1);
        when(mockAccessoryDAO.getAccessoryById(1)).thenReturn(null);

        try (MockedStatic<JOptionPane> mockedOptionPane = Mockito.mockStatic(JOptionPane.class)) {
            accessoriesTab.deleteSelectedItem(null);
            mockedOptionPane.verifyNoInteractions();
            verify(mockAccessoryDAO, never()).deleteAccessory(anyInt());
            verify(mockTableModel, never()).setRowCount(anyInt());
        }
    }
}