/* $Header: /home/gbsmith/projects/MacResReader/ResCafe1.1/src/plugins/RCS/SICNResourceHandler.java,v 1.3 1999/10/21 21:48:10 gbsmith Exp $ */

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.image.*;

import ResourceManager.*;

/*=======================================================================*/
/*
 * $Log: SICNResourceHandler.java,v $
 * Revision 1.3  1999/10/21 21:48:10  gbsmith
 * Added Copyright notice. Changed color model call.
 *
 * Revision 1.2  1999/10/04 22:07:04  gbsmith
 * Adapted to new init technique.
 *
 * Revision 1.1  1999/09/30 05:22:52  gbsmith
 * Initial revision
 *
 */

/*=======================================================================*/
/* Copyright (c) 1999 by G. Brannon Smith -- All Rights Reserved         */
/*=======================================================================*/

/*=======================================================================*/
public class SICNResourceHandler extends GBS_ImageResourceHandler
{
   /*--- Data -----------------------------------------------------------*/
   JList resList;
   JTable resTable;
   private static final String[] columnNames = { "ResID", "Name", "Size", "Icon" };
   IndexColorModel icm;

   TableCellRenderer renderer = new IconRenderer();

   /*------ RCS ---------------------------------------------------------*/
   static final String rcsid = "$Id: SICNResourceHandler.java,v 1.3 1999/10/21 21:48:10 gbsmith Exp $";

   /*--- Methods --------------------------------------------------------*/
   public String[] getTypes()
   {
      return new String[]{"SICN"};
   }

   /*--------------------------------------------------------------------*/
   public void init( )
   {
      int i, j, b;
      MemoryImageSource mis;
      byte rawData[];
      byte iconData[];

      Resource myResArray[] = resData.getResArray();

      icm = MacStandard16Palette.getColorModel();

      myimages = new Image[myResArray.length];

      for( i = 0; i < myResArray.length; i++)
      {
         rawData = myResArray[i].getData();

         // Grab icon data
         iconData = new byte[256];
         for ( j = 0; j < 32; j++)
            for ( b = 0; b < 8; b++)
               iconData[j*8+b] = (byte)((rawData[j] &
                                        (0x80 >>> b)) > 0? 0x0F: 0x00);

         mis = new MemoryImageSource(16, 16, icm, iconData, 0, 16);
         myimages[i] = createImage(mis);
      }
   }

   /*--------------------------------------------------------------------*/
   public void display( )
   {
      int i;
      TableColumn tc;
      DefaultTableModel tmpModel;

      Resource myResArray[] = resData.getResArray();

      setLayout(new BorderLayout());

      tmpModel = new DefaultTableModel(columnNames, myResArray.length);

      for( i = 0; i < myResArray.length; i++)
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
