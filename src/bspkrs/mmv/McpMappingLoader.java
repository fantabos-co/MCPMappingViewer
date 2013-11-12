/*
 * Copyright (C) 2013 Alex "immibis" Campbell
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation 
 * files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, 
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the 
 * Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO 
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, 
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package bspkrs.mmv;

import immibis.bon.IProgressListener;
import immibis.bon.gui.Side;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.TreeMap;

import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

public class McpMappingLoader
{
    
    public static class CantLoadMCPMappingException extends Exception
    {
        private static final long serialVersionUID = 1;
        
        public CantLoadMCPMappingException(String reason)
        {
            super(reason);
        }
    }
    
    private final Side                  side;
    @SuppressWarnings("unused")
    private final String                mcVer;
    private final File                  mcpDir;
    private final File                  srgFile;
    //    private final File                  excFile;
    private SrgFile                     srgFileData;
    private CsvFile                     csvFieldData, csvMethodData;
    
    private Map<MethodSrgData, CsvData> srg2csvMethods = new TreeMap<MethodSrgData, CsvData>();
    private Map<FieldSrgData, CsvData>  srg2csvFields  = new TreeMap<FieldSrgData, CsvData>();
    
    //    @SuppressWarnings("unused")
    //    private ExcFile                     excFileData;
    
    public McpMappingLoader(String mcVer, Side side, File mcpDir, IProgressListener progress) throws IOException, CantLoadMCPMappingException
    {
        this.mcVer = mcVer;
        this.mcpDir = mcpDir;
        this.side = side;
        
        switch (side)
        {
            case Universal:
                if (new File(mcpDir, "conf/packaged.srg").exists())
                {
                    srgFile = new File(mcpDir, "conf/packaged.srg");
                    //excFile = new File(mcpDir, "conf/packaged.exc");
                }
                else
                {
                    srgFile = new File(mcpDir, "conf/joined.srg");
                    //excFile = new File(mcpDir, "conf/joined.exc");
                }
                break;
            
            case Client:
                srgFile = new File(mcpDir, "conf/client.srg");
                //excFile = new File(mcpDir, "conf/client.exc");
                break;
            
            case Server:
                srgFile = new File(mcpDir, "conf/server.srg");
                //excFile = new File(mcpDir, "conf/server.exc");
                break;
            
            default:
                throw new AssertionError("side is " + side);
        }
        
        if (progress != null)
            progress.setMax(4);
        if (progress != null)
            progress.set(0);
        loadEXCFile();
        if (progress != null)
            progress.set(1);
        loadCSVMapping();
        if (progress != null)
            progress.set(2);
        loadSRGMapping();
        if (progress != null)
            progress.set(3);
        linkSrgDataToCsvData();
    }
    
    private void loadEXCFile() throws IOException
    {
        //        excFileData = new ExcFile(excFile);
    }
    
    private void loadSRGMapping() throws IOException
    {
        srgFileData = new SrgFile(srgFile);
    }
    
    private void loadCSVMapping() throws IOException
    {
        csvFieldData = new CsvFile(new File(mcpDir, "conf/fields.csv"), side);
        csvMethodData = new CsvFile(new File(mcpDir, "conf/methods.csv"), side);
    }
    
    private void linkSrgDataToCsvData()
    {
        for (Entry<String, MethodSrgData> methodData : srgFileData.methods.entrySet())
        {
            if (!srg2csvMethods.containsKey(methodData.getValue()) && csvMethodData.data.containsKey(methodData.getKey()))
            {
                srg2csvMethods.put(methodData.getValue(), csvMethodData.data.get(methodData.getKey()));
            }
            else if (srg2csvMethods.containsKey(methodData.getValue()))
                System.out.println("SRG method " + methodData.getKey() + " has multiple entries in CSV file!");
        }
        
        for (Entry<String, FieldSrgData> fieldData : srgFileData.fields.entrySet())
        {
            if (!srg2csvFields.containsKey(fieldData.getValue()) && csvFieldData.data.containsKey(fieldData.getKey()))
            {
                srg2csvFields.put(fieldData.getValue(), csvFieldData.data.get(fieldData.getKey()));
            }
            else if (srg2csvFields.containsKey(fieldData.getValue()))
                System.out.println("SRG field " + fieldData.getKey() + " has multiple entries in CSV file!");
        }
    }
    
    public TableModel getClassModel()
    {
        return null;
    }
    
    public TableModel getMethodModel(String srgPkgAndOwner)
    {
        return null;
    }
    
    public TableModel getFieldModel(String srgPkgAndOwner)
    {
        return null;
    }
    
    public static String getMCVer(File mcpDir) throws IOException
    {
        try (Scanner in = new Scanner(new File(mcpDir, "conf/version.cfg")))
        {
            while (in.hasNextLine())
            {
                String line = in.nextLine();
                if (line.startsWith("ClientVersion"))
                    return line.split("=")[1].trim();
            }
            return "unknown";
        }
    }
    
    public File getMcpDir()
    {
        return this.mcpDir;
    }
    
    public class ClassModel extends AbstractTableModel
    {
        private static final long serialVersionUID = 1L;
        private String[]          columnNames      = { "Pkg name", "SRG name", "Obf name" };
        
        public ClassModel(Map<String, ClassSrgData> map)
        {   
            
        }
        
        @Override
        public int getRowCount()
        {
            return srgFileData.classes.size();
        }
        
        @Override
        public int getColumnCount()
        {
            return columnNames.length;
        }
        
        @Override
        public String getColumnName(int columnIndex)
        {
            if (columnIndex < columnNames.length && columnIndex <= 0)
                return columnNames[columnIndex];
            else
                return "";
        }
        
        @Override
        public Class<?> getColumnClass(int columnIndex)
        {
            return String.class;
        }
        
        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex)
        {
            return false;
        }
        
        @Override
        public Object getValueAt(int rowIndex, int columnIndex)
        {
            return null;
        }
        
        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex)
        {   
            
        }
    }
    
    public class FieldModel extends AbstractTableModel
    {
        private static final long serialVersionUID = 1L;
        
        @Override
        public int getRowCount()
        {
            return 0;
        }
        
        @Override
        public int getColumnCount()
        {
            return 0;
        }
        
        @Override
        public String getColumnName(int columnIndex)
        {
            return null;
        }
        
        @Override
        public Class<?> getColumnClass(int columnIndex)
        {
            return null;
        }
        
        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex)
        {
            return false;
        }
        
        @Override
        public Object getValueAt(int rowIndex, int columnIndex)
        {
            return null;
        }
        
        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex)
        {   
            
        }
        
        @Override
        public void addTableModelListener(TableModelListener l)
        {   
            
        }
        
        @Override
        public void removeTableModelListener(TableModelListener l)
        {   
            
        }
    }
    
    public class MethodModel extends AbstractTableModel
    {
        private static final long serialVersionUID = 1L;
        
        @Override
        public int getRowCount()
        {
            return 0;
        }
        
        @Override
        public int getColumnCount()
        {
            return 0;
        }
        
        @Override
        public String getColumnName(int columnIndex)
        {
            return null;
        }
        
        @Override
        public Class<?> getColumnClass(int columnIndex)
        {
            return null;
        }
        
        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex)
        {
            return false;
        }
        
        @Override
        public Object getValueAt(int rowIndex, int columnIndex)
        {
            return null;
        }
        
        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex)
        {   
            
        }
        
        @Override
        public void addTableModelListener(TableModelListener l)
        {   
            
        }
        
        @Override
        public void removeTableModelListener(TableModelListener l)
        {   
            
        }
    }
}
