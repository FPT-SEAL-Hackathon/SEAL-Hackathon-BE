const fs = require('fs');
let content = fs.readFileSync('database/Create Database.sql', 'utf16le');

content = content.replace(
    /	\[ComputedAt\] \[datetime2\]\(7\) NOT NULL,\r?\n	\[IsPublished\] \[bit\] NOT NULL,\r?\nPRIMARY KEY CLUSTERED/g,
    "\t[ComputedAt] [datetime2](7) NOT NULL,\r\n\t[IsPublished] [bit] NOT NULL,\r\n\t[IsApproved] [bit] NOT NULL DEFAULT 0,\r\nPRIMARY KEY CLUSTERED"
);

content = content.replace(
    /	\[IsPublished\] \[bit\] NOT NULL,\r?\nPRIMARY KEY CLUSTERED/g,
    "\t[IsPublished] [bit] NOT NULL,\r\n\t[IsApproved] [bit] NOT NULL DEFAULT 0,\r\nPRIMARY KEY CLUSTERED"
);

fs.writeFileSync('database/Create Database.sql', content, 'utf16le');
