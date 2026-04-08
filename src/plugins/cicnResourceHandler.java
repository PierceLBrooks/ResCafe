/* $Header: /home/gbsmith/projects/MacResReader/ResCafe1.1/src/plugins/RCS/cicnResourceHandler.java,v 1.3 1999/10/21 22:29:04 gbsmith Exp $ */

import com.sun.jimi.core.Jimi; // JIMI - tools for image I/O

import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import java.awt.BorderLayout;
import java.awt.Image;

import java.io.ByteArrayInputStream;
import java.io.File;

import ResourceManager.*;

/*=======================================================================*/
/*
 * $Log: cicnResourceHandler.java,v $
 * Revision 1.3  1999/10/21 22:29:04  gbsmith
 * Added Copyright notice.
 *
 * Revision 1.2  1999/10/17 23:10:16  gbsmith
 * Made class imports more explicit.
 * Some plain reformatting and reindentation.
 *
 * Revision 1.1  1999/10/17 19:37:43  gbsmith
 * Initial revision
 *
 */

/*=======================================================================*/
/* Copyright (c) 1999 by G. Brannon Smith -- All Rights Reserved         */
/*=======================================================================*/

/*=======================================================================*/
public class cicnResourceHandler extends MacResourceHandler
{
   /*--- Data -----------------------------------------------------------*/
   JList resList;
   JTable resTable;

   private static final String[] columnNames = {
      "ResID", "Name", "Size", "Icon", "Bitmap", "Mask" };

   Image icons[];
   Image bitmaps[];
   Image masks[];

   TableCellRenderer renderer = new IconRenderer();
   cicnParser mycp;

   /*------ RCS ---------------------------------------------------------*/
   static final String rcsid = "$Id: cicnResourceHandler.java,v 1.3 1999/10/21 22:29:04 gbsmith Exp $";

   /*--- Methods --------------------------------------------------------*/
   public String[] getTypes()
   {
      return new String[]{"cicn"};
   }

   /*--------------------------------------------------------------------*/
   public void init( )
   {
      int i, j, b;
      mycp = new cicnParser();
      ByteArrayInputStream bais;
      byte rawData[];

      Resource myResArray[] = resData.getResArray();

      icons = new Image[myResArray.length];
      bitmaps = new Image[myResArray.length];
      masks = new Image[myResArray.length];

      for( i = 0; i < myResArray.length; i++)
      {
         rawData = myResArray[i].getData();
         bais = new ByteArrayInputStream(rawData);
         try
         {
            mycp.read(bais);
            bitmaps[i] = mycp.getBitmap();
            masks[i]   = mycp.getMask();
            icons[i]   = mycp.getIcon();
         } catch (Exception ignore) {}
      }
   }

   /*--------------------------------------------------------------------*/
   public void display( )
   {
      int i, j, b;
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

         tmpModel.setValueAt(icons[i],   i, 3);
         tmpModel.setValueAt(bitmaps[i], i, 4);
         tmpModel.setValueAt(masks[i],   i, 5);
      }

      resTable = new JTable(tmpModel);
      resTable.setRowHeight(36);
      tc = resTable.getColumn("Icon");
      tc.setCellRenderer(renderer);

      tc = resTable.getColumn("Bitmap");
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

      // Bitmaps
      for(int i=0; i < myResArray.length; i++)
      {
         tmpfilename = new StringBuffer(savedir.getPath());
         tmpfilename.append( File.separator + myResArray[i].getID() );
         if(myResArray[i].getName() != null)
            tmpfilename.append("_" + myResArray[i].getName());
         tmpfilename.append("_bitmap");
         tmpfilename.append(".xpm");
         filename = tmpfilename.toString().replace(' ', '_');
         //System.out.println("\tSaving \'" + filename + "\'...");

         try
         {
            Jimi.putImage(bitmaps[i], filename);
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
