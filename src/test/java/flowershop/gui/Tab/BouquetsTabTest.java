package flowershop.gui.Tab;

import flowershop.dao.AccessoryDAO;
import flowershop.dao.BouquetDAO;
import flowershop.dao.FlowerDAO;
import flowershop.gui.Dialog.AddEditBouquetDialog;
import flowershop.models.Accessory;
import flowershop.models.Bouquet;
import flowershop.models.Flower;
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
class BouquetsTabTest {

    @Mock
    private BouquetDAO mockBouquetDAO;
    @Mock
    private FlowerDAO mockFlowerDAO;
    @Mock
    private AccessoryDAO mockAccessoryDAO;

    private BouquetsTab bouquetsTab;

    @Mock
    private JTable mockItemsTable;
    @Mock
    private DefaultTableModel mockTableModel;
    @Mock
    private TableColumnModel mockTableColumnModel;
    @Mock
    private TableColumn mockTableColumn;

    @Mock
    private JSpinner mockMinPriceSpinner;
    @Mock
    private JSpinner mockMaxPriceSpinner;
    @Mock
    private JSpinner mockMinDiscountSpinner;
    @Mock
    private JSpinner mockMaxDiscountSpinner;
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
        bouquetsTab = new BouquetsTab();

        replaceFieldWithReflection(bouquetsTab, "itemDAO", mockBouquetDAO, AbstractItemTab.class);
        replaceFieldWithReflection(bouquetsTab, "flowerDAO", mockFlowerDAO, BouquetsTab.class);
        replaceFieldWithReflection(bouquetsTab, "accessoryDAO", mockAccessoryDAO, BouquetsTab.class);

        replaceFieldWithReflection(bouquetsTab, "itemsTable", mockItemsTable, AbstractItemTab.class);
        replaceFieldWithReflection(bouquetsTab, "tableModel", mockTableModel, AbstractItemTab.class);

        lenient().when(mockItemsTable.getColumnModel()).thenReturn(mockTableColumnModel);
        lenient().when(mockTableColumnModel.getColumn(anyInt())).thenReturn(mockTableColumn);

        replaceFieldWithReflection(bouquetsTab, "minPriceSpinner", mockMinPriceSpinner, BouquetsTab.class);
        replaceFieldWithReflection(bouquetsTab, "maxPriceSpinner", mockMaxPriceSpinner, BouquetsTab.class);
        replaceFieldWithReflection(bouquetsTab, "minDiscountSpinner", mockMinDiscountSpinner, BouquetsTab.class);
        replaceFieldWithReflection(bouquetsTab, "maxDiscountSpinner", mockMaxDiscountSpinner, BouquetsTab.class);

        replaceFieldWithReflection(bouquetsTab, "clearFiltersButton", mockClearFiltersButton, AbstractItemTab.class);
        replaceFieldWithReflection(bouquetsTab, "addButton", mockAddButton, AbstractItemTab.class);
        replaceFieldWithReflection(bouquetsTab, "editButton", mockEditButton, AbstractItemTab.class);

        try {
            Field searchFieldRef = AbstractItemTab.class.getDeclaredField("searchField");
            searchFieldRef.setAccessible(true);
            searchField = (JTextField) searchFieldRef.get(bouquetsTab);
            if (searchField != null) {
                searchField.setText("");
            } else {
                System.err.println("Warning: bouquetsTab.searchField is null in setUp.");
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail("Could not access searchField: " + e.getMessage());
        }

        lenient().when(mockBouquetDAO.getAllBouquets()).thenReturn(new ArrayList<>());
        lenient().when(mockTableModel.getRowCount()).thenReturn(0);

        lenient().when(mockMinPriceSpinner.getValue()).thenReturn(0.0);
        lenient().when(mockMaxPriceSpinner.getValue()).thenReturn(5000.0);
        lenient().when(mockMinDiscountSpinner.getValue()).thenReturn(0.0);
        lenient().when(mockMaxDiscountSpinner.getValue()).thenReturn(50.0);
    }

    private void replaceFieldWithReflection(Object targetObject, String fieldName, Object newValue, Class<?> targetClass) {
        try {
            Field field = targetClass.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(targetObject, newValue);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail("Could not set field '" + fieldName + "' in " + targetClass.getSimpleName() + " via reflection: " + e.getMessage());
        }
    }

    @Test
    void constructor_initializesDAOs() {
        try {
            Field itemDaoField = AbstractItemTab.class.getDeclaredField("itemDAO");
            itemDaoField.setAccessible(true);
            assertSame(mockBouquetDAO, itemDaoField.get(bouquetsTab), "BouquetDAO should be mocked.");

            Field flowerDaoField = BouquetsTab.class.getDeclaredField("flowerDAO");
            flowerDaoField.setAccessible(true);
            assertSame(mockFlowerDAO, flowerDaoField.get(bouquetsTab), "FlowerDAO should be mocked.");

            Field accessoryDaoField = BouquetsTab.class.getDeclaredField("accessoryDAO");
            accessoryDaoField.setAccessible(true);
            assertSame(mockAccessoryDAO, accessoryDaoField.get(bouquetsTab), "AccessoryDAO should be mocked.");

        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail("Could not access DAO fields for verification: " + e.getMessage());
        }
    }

    @Test
    void getTabTitle_returnsCorrectTitle() {
        assertEquals("Управління букетами", bouquetsTab.getTabTitle());
    }

    @Test
    void getColumnNames_returnsCorrectNames() {
        assertArrayEquals(new String[]{"ID", "Назва", "К-сть квітів", "К-сть аксес.", "Ціна", "Ціна зі знижкою", "Знижка (%)"}, bouquetsTab.getColumnNames());
    }

    @Test
    void getColumnClasses_returnsCorrectClasses() {
        assertArrayEquals(new Class<?>[]{Integer.class, String.class, Integer.class, Integer.class, Double.class, Double.class, String.class}, bouquetsTab.getColumnClasses());
    }

    @Test
    void configureTableColumnWidths_setsPreferredWidthsAndRenderers() {
        TableColumn mockIdCol = mock(TableColumn.class);
        TableColumn mockNameCol = mock(TableColumn.class);
        TableColumn mockFlowerCountCol = mock(TableColumn.class);
        TableColumn mockAccCountCol = mock(TableColumn.class);
        TableColumn mockPriceCol = mock(TableColumn.class);
        TableColumn mockDiscPriceCol = mock(TableColumn.class);
        TableColumn mockDiscountCol = mock(TableColumn.class);

        when(mockTableColumnModel.getColumn(0)).thenReturn(mockIdCol);
        when(mockTableColumnModel.getColumn(1)).thenReturn(mockNameCol);
        when(mockTableColumnModel.getColumn(2)).thenReturn(mockFlowerCountCol);
        when(mockTableColumnModel.getColumn(3)).thenReturn(mockAccCountCol);
        when(mockTableColumnModel.getColumn(4)).thenReturn(mockPriceCol);
        when(mockTableColumnModel.getColumn(5)).thenReturn(mockDiscPriceCol);
        when(mockTableColumnModel.getColumn(6)).thenReturn(mockDiscountCol);

        bouquetsTab.configureTableColumnWidths();

        verify(mockIdCol).setPreferredWidth(30);
        verify(mockNameCol).setPreferredWidth(200);
        verify(mockFlowerCountCol).setPreferredWidth(80);
        verify(mockAccCountCol).setPreferredWidth(90);
        verify(mockPriceCol).setPreferredWidth(100);
        verify(mockDiscPriceCol).setPreferredWidth(120);
        verify(mockDiscountCol).setPreferredWidth(80);

        verify(mockIdCol).setCellRenderer(any(DefaultTableCellRenderer.class));
        verify(mockFlowerCountCol).setCellRenderer(any(DefaultTableCellRenderer.class));
        verify(mockAccCountCol).setCellRenderer(any(DefaultTableCellRenderer.class));
        verify(mockDiscountCol).setCellRenderer(any(DefaultTableCellRenderer.class));
        verify(mockPriceCol).setCellRenderer(any(DefaultTableCellRenderer.class));
        verify(mockDiscPriceCol).setCellRenderer(any(DefaultTableCellRenderer.class));
    }

    @Test
    void createFilterPanel_createsPanelWithRows() {
        BouquetsTab realBouquetsTab = new BouquetsTab();

        JPanel filterPanel = realBouquetsTab.createFilterPanel();
        assertNotNull(filterPanel);
        assertTrue(filterPanel.getLayout() instanceof BoxLayout);
        assertEquals(2, filterPanel.getComponentCount());

        Component searchRowComponent = filterPanel.getComponent(0);
        assertTrue(searchRowComponent instanceof JPanel);
        JPanel searchRowPanel = (JPanel) searchRowComponent;
        assertTrue(Arrays.stream(searchRowPanel.getComponents()).anyMatch(c -> c instanceof JLabel && ((JLabel)c).getText().startsWith("Пошук")));
        assertTrue(Arrays.stream(searchRowPanel.getComponents()).anyMatch(c -> c instanceof JTextField));

        Component rangeRowComponent = filterPanel.getComponent(1);
        assertTrue(rangeRowComponent instanceof JPanel);
        JPanel rangeRowPanel = (JPanel) rangeRowComponent;
        assertTrue(Arrays.stream(rangeRowPanel.getComponents()).anyMatch(c -> c instanceof JPanel && ((JPanel)c).getBorder() instanceof TitledBorder && ((TitledBorder)((JPanel)c).getBorder()).getTitle().contains("Загальна ціна")));
        assertTrue(Arrays.stream(rangeRowPanel.getComponents()).anyMatch(c -> c instanceof JPanel && ((JPanel)c).getBorder() instanceof TitledBorder && ((TitledBorder)((JPanel)c).getBorder()).getTitle().contains("Знижка")));
        assertTrue(Arrays.stream(rangeRowPanel.getComponents()).anyMatch(c -> c instanceof JButton && ((JButton)c).getText().equals("Очистити")));
    }

    @Test
    void filterItems_noFilters_returnsAll() {
        Bouquet b1 = new Bouquet("B1", "", new ArrayList<>(), new ArrayList<>(), "", 0);
        Bouquet b2 = new Bouquet("B2", "", new ArrayList<>(), new ArrayList<>(), "", 0);
        List<Bouquet> allBouquets = Arrays.asList(b1, b2);

        searchField.setText("");
        when(mockMinPriceSpinner.getValue()).thenReturn(0.0);
        when(mockMaxPriceSpinner.getValue()).thenReturn(5000.0);
        when(mockMinDiscountSpinner.getValue()).thenReturn(0.0);
        when(mockMaxDiscountSpinner.getValue()).thenReturn(50.0);

        List<Bouquet> filtered = bouquetsTab.filterItems(new ArrayList<>(allBouquets));
        assertEquals(2, filtered.size());
        assertTrue(filtered.containsAll(allBouquets));
    }

    @Test
    void filterItems_bySearchText_name() {
        Bouquet b1 = new Bouquet("Літній Бриз", "", new ArrayList<>(), new ArrayList<>(), "", 0);
        Bouquet b2 = new Bouquet("Осінній Вальс", "", new ArrayList<>(), new ArrayList<>(), "", 0);
        List<Bouquet> allBouquets = Arrays.asList(b1, b2);

        searchField.setText("бриз");
        when(mockMinPriceSpinner.getValue()).thenReturn(0.0);
        when(mockMaxPriceSpinner.getValue()).thenReturn(5000.0);
        when(mockMinDiscountSpinner.getValue()).thenReturn(0.0);
        when(mockMaxDiscountSpinner.getValue()).thenReturn(50.0);

        List<Bouquet> filtered = bouquetsTab.filterItems(new ArrayList<>(allBouquets));
        assertEquals(1, filtered.size());
        assertEquals("Літній Бриз", filtered.get(0).getName());
    }

    @Test
    void filterItems_byPriceRange() {
        Bouquet cheap = new Bouquet("Дешевий", "", new ArrayList<>(), new ArrayList<>(), "", 0);
        Flower flowerForMid = new Flower(Flower.FlowerType.ROSE, 100.0, 80, 50, "Red", "Origin", false, "img.jpg", 1);
        Bouquet mid = new Bouquet("Середній", "", Collections.singletonList(flowerForMid), new ArrayList<>(), "", 0);
        Flower flowerForExpensive = new Flower(Flower.FlowerType.LILY, 1000.0, 80, 50, "White", "Origin", false, "img.jpg", 1);
        Bouquet expensive = new Bouquet("Дорогий", "", Collections.singletonList(flowerForExpensive), new ArrayList<>(), "", 0);
        List<Bouquet> allBouquets = Arrays.asList(cheap, mid, expensive);

        searchField.setText("");
        when(mockMinPriceSpinner.getValue()).thenReturn(50.0);
        when(mockMaxPriceSpinner.getValue()).thenReturn(150.0);
        when(mockMinDiscountSpinner.getValue()).thenReturn(0.0);
        when(mockMaxDiscountSpinner.getValue()).thenReturn(100.0);

        List<Bouquet> filtered = bouquetsTab.filterItems(new ArrayList<>(allBouquets));
        assertEquals(1, filtered.size());
        assertEquals("Середній", filtered.get(0).getName());
    }

    @Test
    void filterItems_byDiscountRange() {
        Bouquet noDiscount = new Bouquet("Без знижки", "", new ArrayList<>(), new ArrayList<>(), "", 0);
        Bouquet lowDiscount = new Bouquet("Мала знижка", "", new ArrayList<>(), new ArrayList<>(), "", 5);
        Bouquet highDiscount = new Bouquet("Велика знижка", "", new ArrayList<>(), new ArrayList<>(), "", 20);
        List<Bouquet> allBouquets = Arrays.asList(noDiscount, lowDiscount, highDiscount);

        searchField.setText("");
        when(mockMinPriceSpinner.getValue()).thenReturn(0.0);
        when(mockMaxPriceSpinner.getValue()).thenReturn(5000.0);
        when(mockMinDiscountSpinner.getValue()).thenReturn(3.0);
        when(mockMaxDiscountSpinner.getValue()).thenReturn(10.0);

        List<Bouquet> filtered = bouquetsTab.filterItems(new ArrayList<>(allBouquets));
        assertEquals(1, filtered.size());
        assertEquals("Мала знижка", filtered.get(0).getName());
    }

    @Test
    void filterItems_combinedFilters() {
        Flower rose = new Flower(Flower.FlowerType.ROSE, 100.0, 90, 60, "Red", "NL", false, "rose.jpg", 10);
        Flower lily = new Flower(Flower.FlowerType.LILY, 200.0, 85, 70, "White", "PL", false, "lily.jpg", 5);
        Flower fieldFlower = new Flower(Flower.FlowerType.DAISY, 50.0, 95, 40, "Yellow", "UA", false, "daisy.jpg", 20);

        Bouquet b1 = new Bouquet("Троянди Любові", "Опис1", Collections.singletonList(new Flower(rose)), new ArrayList<>(), "p1.jpg", 5);
        Bouquet b2 = new Bouquet("Лілії Ніжності", "Опис2", Collections.singletonList(new Flower(lily)), new ArrayList<>(), "p2.jpg", 15);
        Bouquet b3 = new Bouquet("Польові Квіти", "Опис3", Collections.singletonList(new Flower(fieldFlower)), new ArrayList<>(), "p3.jpg", 0);
        List<Bouquet> allBouquets = Arrays.asList(b1, b2, b3);

        searchField.setText("ов");
        when(mockMinPriceSpinner.getValue()).thenReturn(80.0);
        when(mockMaxPriceSpinner.getValue()).thenReturn(250.0);
        when(mockMinDiscountSpinner.getValue()).thenReturn(0.0);
        when(mockMaxDiscountSpinner.getValue()).thenReturn(10.0);

        List<Bouquet> filtered = bouquetsTab.filterItems(new ArrayList<>(allBouquets));
        assertEquals(1, filtered.size());
        assertEquals("Троянди Любові", filtered.get(0).getName());
    }

    @Test
    void clearFilters_resetsFieldsAndRefreshes() {
        searchField.setText("test");

        bouquetsTab.clearFilters();

        assertEquals("", searchField.getText());
        verify(mockMinPriceSpinner).setValue(0);
        verify(mockMaxPriceSpinner).setValue(5000);
        verify(mockMinDiscountSpinner).setValue(0);
        verify(mockMaxDiscountSpinner).setValue(50);

        verify(mockTableModel, atLeastOnce()).setRowCount(0);
    }

    @Test
    void getAllItemsFromDAO_callsDAO() {
        List<Bouquet> expectedBouquets = Collections.singletonList(new Bouquet());
        when(mockBouquetDAO.getAllBouquets()).thenReturn(expectedBouquets);
        List<Bouquet> actualBouquets = bouquetsTab.getAllItemsFromDAO();
        assertSame(expectedBouquets, actualBouquets);
        verify(mockBouquetDAO).getAllBouquets();
    }

    @Test
    void getItemByIdFromDAO_callsDAO() {
        Bouquet expectedBouquet = new Bouquet();
        expectedBouquet.setId(1);
        when(mockBouquetDAO.getBouquetById(1)).thenReturn(expectedBouquet);
        Bouquet actualBouquet = bouquetsTab.getItemByIdFromDAO(1);
        assertSame(expectedBouquet, actualBouquet);
        verify(mockBouquetDAO).getBouquetById(1);
    }

    @Test
    void showAddEditDialog_addMode_showsDialogAndSaves() {
        ActionEvent mockEvent = mock(ActionEvent.class);
        when(mockEvent.getSource()).thenReturn(mockAddButton);

        Bouquet newBouquet = new Bouquet("Новий Букет", "", new ArrayList<>(), new ArrayList<>(), "", 0);
        newBouquet.setId(99);

        when(mockFlowerDAO.getAllFlowers()).thenReturn(new ArrayList<>());
        when(mockAccessoryDAO.getAllAccessories()).thenReturn(new ArrayList<>());

        try (MockedConstruction<AddEditBouquetDialog> mockedDialogConstruction = Mockito.mockConstruction(
                AddEditBouquetDialog.class,
                (mockDialog, context) -> {
                    assertNull(context.arguments().get(1), "Dialog should be created with null bouquet for add mode");
                    assertSame(mockFlowerDAO.getAllFlowers(), context.arguments().get(2));
                    assertSame(mockAccessoryDAO.getAllAccessories(), context.arguments().get(3));
                    when(mockDialog.isSaved()).thenReturn(true);
                    when(mockDialog.getBouquet()).thenReturn(newBouquet);
                })) {

            bouquetsTab.showAddEditDialog(mockEvent);

            assertEquals(1, mockedDialogConstruction.constructed().size());
            AddEditBouquetDialog constructedDialog = mockedDialogConstruction.constructed().get(0);
            verify(constructedDialog).setVisible(true);

            verify(mockBouquetDAO).saveBouquet(newBouquet);
            verify(mockTableModel, atLeastOnce()).setRowCount(0);
        }
    }

    @Test
    void showAddEditDialog_editMode_showsDialogAndSaves() {
        ActionEvent mockEvent = mock(ActionEvent.class);
        when(mockEvent.getSource()).thenReturn(mockEditButton);

        Bouquet bouquetToEdit = new Bouquet("Старий Букет", "Опис", new ArrayList<>(), new ArrayList<>(), "img.jpg", 10);
        bouquetToEdit.setId(123);
        Bouquet editedBouquet = new Bouquet("Оновлений Букет", "Новий опис", new ArrayList<>(), new ArrayList<>(), "new_img.jpg", 15);
        editedBouquet.setId(123);

        when(mockItemsTable.getSelectedRow()).thenReturn(0);
        when(mockItemsTable.convertRowIndexToModel(0)).thenReturn(0);

        lenient().when(mockTableModel.getRowCount()).thenReturn(1);
        lenient().when(mockTableModel.getValueAt(0, 0)).thenReturn(123);
        when(mockBouquetDAO.getBouquetById(123)).thenReturn(bouquetToEdit);

        when(mockFlowerDAO.getAllFlowers()).thenReturn(new ArrayList<>());
        when(mockAccessoryDAO.getAllAccessories()).thenReturn(new ArrayList<>());

        try (MockedConstruction<AddEditBouquetDialog> mockedDialogConstruction = Mockito.mockConstruction(
                AddEditBouquetDialog.class,
                (mockDialog, context) -> {
                    assertSame(bouquetToEdit, context.arguments().get(1), "Dialog should be created with the bouquet to edit");
                    when(mockDialog.isSaved()).thenReturn(true);
                    when(mockDialog.getBouquet()).thenReturn(editedBouquet);
                })) {

            bouquetsTab.showAddEditDialog(mockEvent);

            assertEquals(1, mockedDialogConstruction.constructed().size());
            AddEditBouquetDialog constructedDialog = mockedDialogConstruction.constructed().get(0);
            verify(constructedDialog).setVisible(true);

            verify(mockBouquetDAO).saveBouquet(editedBouquet);
            verify(mockTableModel, atLeastOnce()).setRowCount(0);
        }
    }

    @Test
    void showAddEditDialog_editMode_noSelection_showsWarning() {
        ActionEvent mockEvent = mock(ActionEvent.class);
        when(mockEvent.getSource()).thenReturn(mockEditButton);
        when(mockItemsTable.getSelectedRow()).thenReturn(-1);

        try (MockedStatic<JOptionPane> mockedOptionPane = Mockito.mockStatic(JOptionPane.class)) {
            bouquetsTab.showAddEditDialog(mockEvent);
            mockedOptionPane.verify(() -> JOptionPane.showMessageDialog(
                    eq(bouquetsTab),
                    eq("Будь ласка, оберіть букет для редагування."),
                    eq("Попередження"),
                    eq(JOptionPane.WARNING_MESSAGE)
            ));
        }
    }

    @Test
    void showAddEditDialog_dialogCancelled_doesNotSave() {
        ActionEvent mockEvent = mock(ActionEvent.class);
        when(mockEvent.getSource()).thenReturn(mockAddButton);

        when(mockFlowerDAO.getAllFlowers()).thenReturn(new ArrayList<>());
        when(mockAccessoryDAO.getAllAccessories()).thenReturn(new ArrayList<>());

        try (MockedConstruction<AddEditBouquetDialog> mockedDialogConstruction = Mockito.mockConstruction(
                AddEditBouquetDialog.class,
                (mockDialog, context) -> {
                    when(mockDialog.isSaved()).thenReturn(false);
                })) {
            bouquetsTab.showAddEditDialog(mockEvent);
            verify(mockBouquetDAO, never()).saveBouquet(any(Bouquet.class));
            verify(mockTableModel, never()).setRowCount(anyInt());
        }
    }

    @Test
    void selectRowByBouquetId_bouquetExists() {
        when(mockItemsTable.getRowCount()).thenReturn(1);
        when(mockItemsTable.convertRowIndexToModel(0)).thenReturn(0);

        Bouquet bouquetInTable = new Bouquet("Test Bouquet", "", new ArrayList<>(), new ArrayList<>(), "", 0);
        bouquetInTable.setId(777);

        lenient().when(mockTableModel.getRowCount()).thenReturn(1);
        lenient().when(mockTableModel.getValueAt(0, 0)).thenReturn(777);
        when(mockBouquetDAO.getBouquetById(777)).thenReturn(bouquetInTable);

        lenient().when(mockItemsTable.getCellRect(anyInt(), anyInt(), anyBoolean())).thenReturn(new Rectangle(0, 0, 1, 1));

        bouquetsTab.selectRowByBouquetId(777);

        verify(mockItemsTable).setRowSelectionInterval(0, 0);
        verify(mockItemsTable).scrollRectToVisible(any(Rectangle.class));
    }

    @Test
    void selectRowByBouquetId_bouquetNotExists_doesNotSelect() {
        when(mockItemsTable.getRowCount()).thenReturn(1);
        when(mockItemsTable.convertRowIndexToModel(0)).thenReturn(0);

        Bouquet otherBouquet = new Bouquet("Other", "", new ArrayList<>(), new ArrayList<>(), "", 0);
        otherBouquet.setId(111);
        lenient().when(mockTableModel.getRowCount()).thenReturn(1);
        lenient().when(mockTableModel.getValueAt(0, 0)).thenReturn(111);
        when(mockBouquetDAO.getBouquetById(111)).thenReturn(otherBouquet);

        bouquetsTab.selectRowByBouquetId(777);

        verify(mockItemsTable, never()).setRowSelectionInterval(anyInt(), anyInt());
    }

    @Test
    void selectRowByBouquetId_emptyTable_doesNotSelect() {
        when(mockItemsTable.getRowCount()).thenReturn(0);
        bouquetsTab.selectRowByBouquetId(777);
        verify(mockItemsTable, never()).setRowSelectionInterval(anyInt(), anyInt());
    }

    @Test
    void selectRowByBouquetId_itemInRowIsNull_doesNotSelect() {
        when(mockItemsTable.getRowCount()).thenReturn(1);
        when(mockItemsTable.convertRowIndexToModel(0)).thenReturn(0);
        lenient().when(mockTableModel.getRowCount()).thenReturn(1);
        lenient().when(mockTableModel.getValueAt(0,0)).thenReturn(777);
        when(mockBouquetDAO.getBouquetById(777)).thenReturn(null);

        bouquetsTab.selectRowByBouquetId(777);
        verify(mockItemsTable, never()).setRowSelectionInterval(anyInt(), anyInt());
    }

    @Test
    void deleteSelectedItem_noSelection_showsWarning() {
        when(mockItemsTable.getSelectedRow()).thenReturn(-1);
        try (MockedStatic<JOptionPane> mockedOptionPane = Mockito.mockStatic(JOptionPane.class)) {
            bouquetsTab.deleteSelectedItem(null);
            mockedOptionPane.verify(() -> JOptionPane.showMessageDialog(
                    eq(bouquetsTab),
                    eq("Будь ласка, оберіть букет для видалення."),
                    eq("Попередження"),
                    eq(JOptionPane.WARNING_MESSAGE)
            ));
        }
    }

    @Test
    void deleteSelectedItem_cancelled_doesNothing() {
        when(mockItemsTable.getSelectedRow()).thenReturn(0);
        when(mockItemsTable.convertRowIndexToModel(0)).thenReturn(0);

        Bouquet bouquetToDelete = new Bouquet("Не видаляти", "", new ArrayList<>(), new ArrayList<>(), "", 0);
        bouquetToDelete.setId(1);
        lenient().when(mockTableModel.getRowCount()).thenReturn(1);
        lenient().when(mockTableModel.getValueAt(0, 0)).thenReturn(1);
        when(mockBouquetDAO.getBouquetById(1)).thenReturn(bouquetToDelete);

        try (MockedStatic<JOptionPane> mockedOptionPane = Mockito.mockStatic(JOptionPane.class)) {
            mockedOptionPane.when(() -> JOptionPane.showConfirmDialog(
                    any(), anyString(), anyString(), eq(JOptionPane.YES_NO_OPTION), eq(JOptionPane.WARNING_MESSAGE)
            )).thenReturn(JOptionPane.NO_OPTION);

            bouquetsTab.deleteSelectedItem(null);

            verify(mockBouquetDAO, never()).deleteBouquet(anyInt());
            verify(mockTableModel, never()).setRowCount(0);
            mockedOptionPane.verify(() -> JOptionPane.showMessageDialog(any(), anyString(), anyString(), anyInt()), never());
        }
    }

    @Test
    void deleteSelectedItem_itemToDeleteIsNull_logsErrorAndDoesNotProceed() {
        when(mockItemsTable.getSelectedRow()).thenReturn(0);
        when(mockItemsTable.convertRowIndexToModel(0)).thenReturn(0);

        lenient().when(mockTableModel.getRowCount()).thenReturn(1);
        lenient().when(mockTableModel.getValueAt(0,0)).thenReturn(1);
        when(mockBouquetDAO.getBouquetById(1)).thenReturn(null);

        try (MockedStatic<JOptionPane> mockedOptionPane = Mockito.mockStatic(JOptionPane.class)) {
            bouquetsTab.deleteSelectedItem(null);
            mockedOptionPane.verifyNoInteractions();
            verify(mockBouquetDAO, never()).deleteBouquet(anyInt());
            verify(mockTableModel, never()).setRowCount(anyInt());
        }
    }

    @Test
    void getDetailsPanelTitle_returnsCorrectTitle() {
        assertEquals("Деталі букета", bouquetsTab.getDetailsPanelTitle());
    }

    @Test
    void getDetailedInfoForItem_withBouquet_returnsHtmlFromBouquet() {
        Bouquet mockBouquet = mock(Bouquet.class);
        String expectedHtml = "<html><body>Детальна інформація про букет</body></html>";
        when(mockBouquet.getDetailedInfo()).thenReturn(expectedHtml);

        String actualHtml = bouquetsTab.getDetailedInfoForItem(mockBouquet);
        assertEquals(expectedHtml, actualHtml);
        verify(mockBouquet).getDetailedInfo();
    }

    @Test
    void getDetailedInfoForItem_withNullBouquet_returnsEmptyString() {
        assertEquals("", bouquetsTab.getDetailedInfoForItem(null));
    }

    @Test
    void getImagePathForItem_withBouquet_returnsPathFromBouquet() {
        Bouquet mockBouquet = mock(Bouquet.class);
        String expectedPath = "path/to/image.jpg";
        when(mockBouquet.getImagePath()).thenReturn(expectedPath);

        assertEquals(expectedPath, bouquetsTab.getImagePathForItem(mockBouquet));
        verify(mockBouquet).getImagePath();
    }

    @Test
    void getImagePathForItem_withNullBouquet_returnsNull() {
        assertNull(bouquetsTab.getImagePathForItem(null));
    }

    @Test
    void hasStockLevelBar_returnsFalse() {
        assertFalse(bouquetsTab.hasStockLevelBar());
    }

    @Test
    void updateStockLevelBar_doesNothing() {
        assertDoesNotThrow(() -> bouquetsTab.updateStockLevelBar(new Bouquet()));
        assertDoesNotThrow(() -> bouquetsTab.updateStockLevelBar(null));
    }

    @Test
    void getItemNameSingular_returnsCorrectName() {
        assertEquals("букет", bouquetsTab.getItemNameSingular());
    }

    @Test
    void searchField_documentChanged_refreshesTable() {
        searchField.setText("new search");
        verify(mockTableModel, atLeastOnce()).setRowCount(0);
    }
}