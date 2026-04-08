/* $Header: /home/gbsmith/projects/MacResReader/ResCafe_1.0/src/RCS/DefaultResourceHandler.java,v 1.3 1999/10/13 23:35:14 gbsmith Exp $ */

import javax.swing.*;
import javax.swing.table.*;

import java.awt.BorderLayout;

import java.io.File;
import java.io.FileOutputStream;

import java.util.Enumeration;

import ResourceManager.*;


/*=======================================================================*/
/*
 * $Log: DefaultResourceHandler.java,v $
 * Revision 1.3  1999/10/13 23:35:14  gbsmith
 * Modified variable names to match updated MacResourceHandler superclass.
 * Also removed raw byte save method since it is now inherited from
 * MacResourceHandler.
 *
 * Revision 1.2  1999/10/04 20:54:27  gbsmith
 * Changed init method to reflect new method of passing data to Handler.
 * Added raw data saving method.
 *
 * Revision 1.1  1999/09/30 05:21:23  gbsmith
 * Initial revision
 */


/*=======================================================================*/
public class DefaultResourceHandler extends MacResourceHandler
{
   /*--- Data -----------------------------------------------------------*/
   private JList  resList;
   private JTable resTable;
   private static final String[] columnNames = { "ResID", "Name", "Size"};

   /*------ RCS ---------------------------------------------------------*/
   static final String rcsid = "$Id: DefaultResourceHandler.java,v 1.3 1999/10/13 23:35:14 gbsmith Exp $";

   /*--- Methods --------------------------------------------------------*/
   public String[] getTypes( )
   {
      return new String[]{"default"};
   }

   /*--------------------------------------------------------------------*/
   public void init( )
   {
   }

   /*--------------------------------------------------------------------*/
   public void display( )
   {
      DefaultTableModel tmpModel;
      Enumeration resIDs;
      Resource currentRes;

      setLayout(new BorderLayout());
      tmpModel = new DefaultTableModel(columnNames, resData.size());

      int i = 0;
      resIDs = resData.getResourceIDs();
      while(resIDs.hasMoreElements())
      {
         currentRes = resData.getResource((Short)resIDs.nextElement());
         tmpModel.setValueAt(new Short(currentRes.getID()),  i, 0);
         tmpModel.setValueAt(currentRes.getName(),           i, 1);
         tmpModel.setValueAt(new Integer(currentRes.size()), i, 2);
         i++;
      }

      resTable = new JTable(tmpModel);
      JScrollPane rtsp = new JScrollPane(resTable);
      add(rtsp, "Center");
   }
}
