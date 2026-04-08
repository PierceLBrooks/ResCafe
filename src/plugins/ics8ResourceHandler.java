/* $Header: /home/gbsmith/projects/MacResReader/ResCafe1.1/src/plugins/RCS/ics8ResourceHandler.java,v 1.3 1999/10/21 22:16:00 gbsmith Exp $ */

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
 * $Log: ics8ResourceHandler.java,v $
 * Revision 1.3  1999/10/21 22:16:00  gbsmith
 * Added Copyright notice. Made class imports more explicit.
 *
 * Revision 1.2  1999/10/04 22:25:31  gbsmith
 * Adapted to new init technique.
 *
 * Revision 1.1  1999/09/30 05:26:34  gbsmith
 * Initial revision
 *
 *
 */

/*=======================================================================*/
/* Copyright (c) 1999 by G. Brannon Smith -- All Rights Reserved         */
/*=======================================================================*/

/*=======================================================================*/
public class ics8ResourceHandler extends GBS_ImageResourceHandler
{
   /*--- Data -----------------------------------------------------------*/
   JList resList;
   JTable resTable;
   private static final String[] columnNames = { "ResID", "Name", "Size", "Icon"};
   IndexColorModel icm;
   TableCellRenderer renderer = new IconRenderer();

   /*------ RCS ------------------------------------------7---------------*/
   static final String rcsid = "$Id: ics8ResourceHandler.java,v 1.3 1999/10/21 22:16:00 gbsmith Exp $";

   /*--- Methods --------------------------------------------------------*/
   public String[] getTypes()
   {
      return new String[]{"ics8"};
   }

   /*--------------------------------------------------------------------*/
   public void init( )
   {
      MemoryImageSource mis;

      Resource myResArray[] = resData.getResArray();

      icm = MacStandard256Palette.getColorModel();

      myimages = new Image[myResArray.length];

      for( int i = 0; i < myResArray.length; i++)
      {
         mis = new MemoryImageSource(16, 16, icm, myResArray[i].getData(), 0, 16);
         myimages[i] = createImage(mis);
      }
   }


   /*--------------------------------------------------------------------*/
   public void display( )
   {
      TableColumn tc;
      DefaultTableModel tmpModel;

      Resource myResArray[] = resData.getResArray();

      setLayout(new BorderLayout());
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
      resTable.setRowHeight(20);
      tc = resTable.getColumn("Icon");
      tc.setCellRenderer(renderer);

      JScrollPane rtsp = new JScrollPane(resTable);
      add(rtsp, "Center");
   }
}
