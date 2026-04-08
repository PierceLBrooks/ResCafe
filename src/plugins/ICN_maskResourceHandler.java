/* $Header: /home/gbsmith/projects/MacResReader/ResCafe1.1/src/plugins/RCS/ICN_maskResourceHandler.java,v 1.3 1999/10/21 21:24:20 gbsmith Exp $ */

import com.sun.jimi.core.Jimi; //  JIMI - tools for image I/O

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

import java.io.File;

import ResourceManager.*;

/*=======================================================================*/
/*
 * $Log: ICN_maskResourceHandler.java,v $
 * Revision 1.3  1999/10/21 21:24:20  gbsmith
 * Added Copyright notice. Made class imports more explicit.
 *
 * Revision 1.2  1999/10/04 22:00:05  gbsmith
 * Added save method and adapted to new init technique.
 */

/*=======================================================================*/
/* Copyright (c) 1999 by G. Brannon Smith -- All Rights Reserved         */
/*=======================================================================*/

/*=======================================================================*/
public class ICN_maskResourceHandler extends MacResourceHandler
{
   /*--- Data -----------------------------------------------------------*/
   JList resList;
   JTable resTable;
   private static final String[] columnNames = {
      "ResID", "Name", "Size", "Icon", "Mask" };
   IndexColorModel icm;
   Image icons[];
   Image masks[];
   TableCellRenderer renderer = new IconRenderer();

   /*------ RCS ---------------------------------------------------------*/
   static final String rcsid = "$Id: ICN_maskResourceHandler.java,v 1.3 1999/10/21 21:24:20 gbsmith Exp $";

   /*--- Methods --------------------------------------------------------*/
   public String[] getTypes()
   {
      return new String[]{"ICN#"};
   }

   /*--------------------------------------------------------------------*/
   public void init( )
   {
      int i, j, b;
      MemoryImageSource mis;
      TableColumn tc;
      byte rawData[];
      byte iconData[];
      byte maskData[];

      Resource myResArray[] = resData.getResArray();

      icm = MacStandard16Palette.getColorModel();

      icons = new Image[myResArray.length];
      masks = new Image[myResArray.length];

      for( i = 0; i < myResArray.length; i++)
      {
         rawData = myResArray[i].getData();

         // Grab icon data
         iconData = new byte[1024];
         for ( j = 0; j < 128; j++)
            for ( b = 0; b < 8; b++)
               iconData[j*8+b] = (byte)((rawData[j] &
                                        (0x80 >>> b)) > 0? 0x0F: 0x00);

         mis = new MemoryImageSource(32, 32, icm, iconData, 0, 32);
         icons[i] = createImage(mis);

         // Grab mask data
         maskData = new byte[1024];
         for ( j = 0; j < 128; j++)
            for ( b = 0; b < 8; b++)
               maskData[j*8+b] = (byte)((rawData[j + 128] &
                                        (0x80 >>> b)) > 0? 0xFF: 0x00);

         mis = new MemoryImageSource(32, 32, icm, maskData, 0, 32);
         masks[i] = createImage(mis);
      }
   }

   /*--------------------------------------------------------------------*/
   public void display( )
   {
      int i, j, b;
      MemoryImageSource mis;
      TableColumn tc;
      DefaultTableModel tmpModel;

      Resource myResArray[] = resData.getResArray();

      setLayout(new BorderLayout());
      tmpModel = new DefaultTableModel(columnNames, myResArray.length);

      for( i = 0; i < myResArray.length; i++)
      {
         tmpModel.setValueAt(new Short(myResArray[i].getID()),  i, 0);
         tmpModel.setValueAt(myResArray[i].getName(),           i, 1);
         tmpModel.setValueAt(new Integer(myResArray[i].size()), i, 2);
         tmpModel.setValueAt(icons[i], i, 3);
         tmpModel.setValueAt(masks[i], i, 4);
      }

      resTable = new JTable(tmpModel);
      resTable.setRowHeight(36);
      tc = resTable.getColumn("Icon");
      tc.setCellRenderer(renderer);

      tc = resTable.getColumn("Mask");
      tc.setCellRenderer(renderer);

      JScrollPane rtsp = new JScrollPane(resTable);
      add(rtsp, "Center");
   }

   /*--------------------------------------------------------------------*/
   public void save ( File savedir )
   {
      StringBuffer tmpfilename;
      String filename;
      String saveType;

      if(resData == null)
      {
         System.err.println("ERROR: No resources to save");
         return;
      }

      Resource myResArray[] = resData.getResArray();
      saveType = resData.getID();

      System.out.println("Saving resources of type \'" + saveType + "\'");
      // Icons
      for(int i=0; i < myResArray.length; i++)
      {
         tmpfilename = new StringBuffer( savedir.getPath() );
         tmpfilename.append( File.separator + myResArray[i].getID() );
         if(myResArray[i].getName() != null)
            tmpfilename.append("_" + myResArray[i].getName());
         tmpfilename.append(".xpm");
         filename = tmpfilename.toString().replace(' ', '_');
         //System.out.println("\tSaving \'" + filename + "\'...");

         try
         {
            Jimi.putImage(icons[i], filename);
         } catch (Exception whatever) {
            System.err.println("ERROR: Got exception " + whatever );
         }
      }

      // Masks
      for(int i=0; i < myResArray.length; i++)
      {
         tmpfilename = new StringBuffer(savedir.getPath());
         tmpfilename.append( File.separator + myResArray[i].getID() );
         if(myResArray[i].getName() != null)
            tmpfilename.append("_" + myResArray[i].getName());
         tmpfilename.append("_mask");
         tmpfilename.append(".xpm");
         filename = tmpfilename.toString().replace(' ', '_');
         //System.out.println("\tSaving \'" + filename + "\'...");

         try
         {
            Jimi.putImage(masks[i], filename);
         } catch (Exception whatever) {
            System.err.println("ERROR: Got exception " + whatever );
         }
      }
   }
}
