/* $Header: /home/gbsmith/projects/ResCafe/ResCafe1.2.5/src/ResourceManager/RCS/Resource.java,v 1.3 1999/10/21 23:47:36 gbsmith Exp $ */

package ResourceManager;

import java.io.IOException;
import java.io.PrintStream;
import java.io.RandomAccessFile;

/*=======================================================================*/
/*
 * $Log: Resource.java,v $
 * Revision 1.3  1999/10/21 23:47:36  gbsmith
 * Added Copyright notice.
 *
 * Revision 1.2  1999/10/04 20:14:42  gbsmith
 * Placed in package ResourceManager. Made class imports more explicit.
 * Added javadoc formatted comments. Made class and accessor methods
 * public since package access now more restrictive.
 *
 * Revision 1.1  1999/09/30 05:26:39  gbsmith
 * Initial revision
 *
 */

/*=======================================================================*/
/* Copyright (c) 1999 by G. Brannon Smith -- All Rights Reserved         */
/*=======================================================================*/

/*=======================================================================*/
/**
 * Resource: Resource reads and holds the binary data of a MacOS Resource
 * and all pertinent information: the ID, name, and size. Public accessor
 * methods are provided to access the info
 * @author Copyright (c) 1999 by George B. Smith
 */
public class Resource
{
   /*--- Data -----------------------------------------------------------*/
   short id;
   short nameOffset;
   byte attr;
   long dataOffset; // 4-byte unsigned
   String name;
   byte data[];

   /*--- RCS ------------------------------------------------------------*/
   static final String rcsid = "$Id: Resource.java,v 1.3 1999/10/21 23:47:36 gbsmith Exp $";

   /*--- Methods --------------------------------------------------------*/
   void readInfo(RandomAccessFile inraf) throws IOException
   {
      id = inraf.readShort();
      nameOffset = inraf.readShort();
      attr = inraf.readByte();
      dataOffset = ResourceIntegers.readRes3Byte(inraf);
      ResourceIntegers.readResLong(inraf); // Skip Handle to Resource
      name = null;
   }

   /*----------------------------------------------------------------------*/
   void readName(RandomAccessFile inraf, long offset) throws IOException
   {
      int len;
      byte tmpbytes[];

      if(nameOffset != -1)
      {
         inraf.seek(offset + nameOffset);
         len = ResourceIntegers.readResUnsignedByte(inraf);
         tmpbytes = new byte[len];
         inraf.read(tmpbytes);
         name = new String(tmpbytes);
      } else name = null;

   }

   /*----------------------------------------------------------------------*/
   void readData(RandomAccessFile inraf, long offset) throws IOException
   {
      long len;

      inraf.seek(offset + dataOffset);
      len = ResourceIntegers.readResLong(inraf);
      data = new byte[(int)len]; // This cast could be a problem...
      inraf.read(data); // Read in Resource Data
   }

   /*----------------------------------------------------------------------*/
   /**
    * Dumps the Resource information (not including data) to a Stream
    * @param ps the PrintStream to print the info
    */
   public void print(PrintStream ps)
   {
      ps.println("Resource: ");
      ps.println("\t        id = " + id);
      ps.println("\tnameOffset = " + nameOffset);
      ps.println("\t      attr = " + attr);
      ps.println("\tdataOffset = " + dataOffset);
      ps.println("\t      name = " + name);
      ps.println("\t       len = " + data.length);
      ps.println("--------------------------------------------------");
      //ps.println("byte data[];
   }

   /**
    * Returns the numeric ID of the Resource
    */
   public short getID()    { return id; }

   /**
    * Returns the size of the Resource data
    */
   public int size()       { return data.length; }

   /**
    * Returns the name of Resource, if any. This could be null
    */
   public String getName() { return name;  }

   /**
    * Returns the raw byte data of the Resource - the heart of the Resource
    */
   public byte[] getData() {return data; }
}
