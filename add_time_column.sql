-- Add time_added column to duplicator table
ALTER TABLE duplicator ADD COLUMN IF NOT EXISTS time_added TIME;

-- Verify the column was added
SELECT COLUMN_NAME, DATA_TYPE FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'DUPLICATOR' AND COLUMN_NAME = 'TIME_ADDED';
