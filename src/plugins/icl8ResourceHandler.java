/* $Header: /home/gbsmith/projects/MacResReader/ResCafe_1.0/src/plugins/RCS/icl8ResourceHandler.java,v 1.2 1999/10/21 22:10:58 gbsmith Exp $ */

import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import java.awt.BorderLayout;
import java.awt.Image;

import java.awt.image.IndexColorModel;
import java.awt.image.MemoryImageSource;

import ResourceManager.*;

/*=======================================================================*/
/*
 * $Log: icl8ResourceHandler.java,v $
 * Revision 1.2  1999/10/21 22:10:58  gbsmith
 * Added Copyright notice. Made class imports more explicit.
 *
 * Revision 1.1  1999/10/04 22:17:39  gbsmith
 * Initial revision
 *
 */

/*=======================================================================*/
/* Copyright (c) 1999 by G. Brannon Smith -- All Rights Reserved         */
/*=======================================================================*/

/*=======================================================================*/
public class icl8ResourceHandler extends GBS_ImageResourceHandler
{
   /*--- Data -----------------------------------------------------------*/
   IndexColorModel icm;

   JList resList;
   JTable resTable;
   private static final String[] columnNames = { "ResID", "Name", "Size", "Icon"};
   TableCellRenderer renderer = new IconRenderer();

   /*------ RCS ---------------------------------------------------------*/
   static final String rcsid = "$Id: icl8ResourceHandler.java,v 1.2 1999/10/21 22:10:58 gbsmith Exp $";

   /*--- Methods --------------------------------------------------------*/
   public String[] getTypes()
   {
      return new String[]{"icl8"};
   }

   /*--------------------------------------------------------------------*/
   public void init( )
   {
      MemoryImageSource mis;
      TableColumn tc;

      Resource myResArray[] = resData.getResArray();

      icm = MacStandard256Palette.getColorModel();

      myimages = new Image[myResArray.length];

      for( int i = 0; i < myResArray.length; i++)
      {
         mis = new MemoryImageSource(32, 32, icm, myResArray[i].getData(), 0, 32);
         myimages[i] = createImage(mis);
      }
   }

   /*--------------------------------------------------------------------*/
   public void display( )
   {
      TableColumn tc;
      DefaultTableModel tmpModel;

      setLayout(new BorderLayout());

      Resource myResArray[] = resData.getResArray();
      tmpModel = new DefaultTableModel(columnNames, myResArray.length);

      for( int i = 0; i < myResArray.length; i++)
      {
         // "ResID", "Name", "Size", "Icon"
         tmpModel.setValueAt(new Short(myResArray[i].getID()),  i, 0);
         tmpModel.setValueAt(myResArray[i].getName(),           i, 1);
         tmpModel.setValueAt(new Integer(myResArray[i].size()), i, 2);
         tmpModel.setValueAt(myimages[i],                       i, 3);
      }

      resTable = new JTable(tmpModel);
      resTable.setRowHeight(36);
      tc = resTable.getColumn("Icon");
      tc.setCellRenderer(renderer);

      JScrollPane rtsp = new JScrollPane(resTable);
      add(rtsp, "Center");
   }
}
