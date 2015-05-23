package com.outlook.corey_young.spellarrows;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MyListenerTest {

	/**
	 * Test of getNumberOfArrowStacks method, of class MyListener.
	 */
	@Test
	public void testGetNumberOfArrowStacks() {
		System.out.println("getNumberOfArrowStacks");

		Inventory inventory1 = mock(Inventory.class);
		when(inventory1.getSize()).thenReturn(9);
		when(inventory1.getItem(0)).thenReturn(new ItemStack(Material.ARROW));
		when(inventory1.getItem(1)).thenReturn(new ItemStack(Material.ARROW));
		when(inventory1.getItem(2)).thenReturn(new ItemStack(Material.ARROW));
		
		Inventory inventory2 = mock(Inventory.class);
		
		MyListener instance = new MyListener();
		int expResult1 = 3;
		int expResult2 = 0;
		int result1 = instance.getNumberOfArrowStacks(inventory1);
		int result2 = instance.getNumberOfArrowStacks(inventory2);

		assertEquals(expResult1, result1);
		assertEquals(expResult2, result2);
	}

	/**
	 * Test of getArrowStacks method, of class MyListener.
	 */
	@Test
	public void testGetArrowStacks() {
		System.out.println("getArrowStacks");

		Inventory inventory = mock(Inventory.class);
		when(inventory.getSize()).thenReturn(9);
		when(inventory.getItem(0)).thenReturn(new ItemStack(Material.ARROW));
		when(inventory.getItem(1)).thenReturn(new ItemStack(Material.ARROW));
		when(inventory.getItem(2)).thenReturn(new ItemStack(Material.ARROW));
		when(inventory.getItem(3)).thenReturn(new ItemStack(Material.ARROW));

		MyListener instance = new MyListener();
		
		ItemStack[] result = instance.getArrowStacks(inventory);
		for (int i = 0; i < 3; i++) {
			assertEquals(result[i].getAmount(), 1);
			assertEquals(result[i].getType(), Material.ARROW);
		}
	}

	/**
	 * Test of getArrowStackIndices method, of class MyListener.
	 */
	@Test
	public void testGetArrowStackIndices() {
		System.out.println("getArrowStackIndices");
		Inventory inventory = mock(Inventory.class);
		when(inventory.getSize()).thenReturn(10);
		when(inventory.getItem(0)).thenReturn(new ItemStack(Material.ARROW));
		when(inventory.getItem(3)).thenReturn(new ItemStack(Material.ARROW));
		when(inventory.getItem(6)).thenReturn(new ItemStack(Material.ARROW));
		when(inventory.getItem(8)).thenReturn(new ItemStack(Material.ARROW));

		MyListener instance = new MyListener();
		int[] expResult = new int[4];
		expResult[0] = 0;
		expResult[1] = 3;
		expResult[2] = 6;
		expResult[3] = 8;

		int[] result = instance.getArrowStackIndices(inventory);
		assertArrayEquals(expResult, result);
	}

}
