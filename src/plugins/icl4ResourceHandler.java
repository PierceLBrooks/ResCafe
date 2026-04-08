/* $Header: /home/gbsmith/projects/MacResReader/ResCafe_1.0/src/plugins/RCS/icl4ResourceHandler.java,v 1.3 1999/10/21 22:00:33 gbsmith Exp $ */


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
 * $Log: icl4ResourceHandler.java,v $
 * Revision 1.3  1999/10/21 22:00:33  gbsmith
 * Added Copyright notice. Made class imports more explicit.
 * Changed a couple of table headers.
 *
 * Revision 1.2  1999/10/04 22:16:33  gbsmith
 * Adapted to new init technique.
 *
 * Revision 1.1  1999/09/30 05:31:09  gbsmith
 * Initial revision
 *
 */

/*=======================================================================*/
/* Copyright (c) 1999 by G. Brannon Smith -- All Rights Reserved         */
/*=======================================================================*/

/*=======================================================================*/
public class icl4ResourceHandler extends GBS_ImageResourceHandler
{
   /*--- Data -----------------------------------------------------------*/
   IndexColorModel icm;

   private static final String[] columnNames = { "ResID", "Name", "Size", "Icon"};
   JList resList;
   JTable resTable;
   TableCellRenderer renderer = new IconRenderer();

   /*------ RCS ---------------------------------------------------------*/
   static final String rcsid = "$Id: icl4ResourceHandler.java,v 1.3 1999/10/21 22:00:33 gbsmith Exp $";

   /*--- Methods --------------------------------------------------------*/
   public String[] getTypes()
   {
      return new String[]{"icl4"};
   }

   /*--------------------------------------------------------------------*/
   public void init( )
   {
      MemoryImageSource mis;
      byte rawData[];
      byte iconData[];

      Resource myResArray[] = resData.getResArray();
      icm = MacStandard16Palette.getColorModel();

      myimages = new Image[myResArray.length];

      for( int i = 0; i < myResArray.length; i++)
      {
         rawData = myResArray[i].getData();
         iconData = new byte[1024];
         for (int j = 0; j < 512; j++)
         {
            // Grab high 4 bytes
            iconData[j*2]   = (byte)((rawData[j] >> 4) & 0x0F);

            // Grab low 4 bytes
            iconData[j*2+1] = (byte)(rawData[j] & 0x0F);
         }

         mis = new MemoryImageSource(32, 32, icm, iconData, 0, 32);
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
      resTable.setRowHeight(36);
      tc = resTable.getColumn("Icon");
      tc.setCellRenderer(renderer);

      JScrollPane rtsp = new JScrollPane(resTable);
      add(rtsp, "Center");
   }
}

