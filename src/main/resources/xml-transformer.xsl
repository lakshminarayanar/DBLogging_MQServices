<xsl:stylesheet version="1.0" 
xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes" cdata-section-elements="Content"/>
<xsl:strip-space elements="*"/>

<!-- identity transform -->
<xsl:template match="@*|node()">
    <xsl:copy>
        <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
</xsl:template>

<xsl:template match="Content">
    <xsl:copy>
        <xsl:variable name="content">
            <xsl:value-of select="substring-before(.,'&lt;DebAccNo&gt;')" />
            <xsl:text>&lt;DebAccNo&gt;</xsl:text>
            <xsl:variable name="acct-num" select="substring-before(substring-after(.,'&lt;DebAccNo&gt;'), '&lt;/DebAccNo&gt;')" />
            <xsl:value-of select="concat('************', substring($acct-num, string-length($acct-num) - 2))" />
            <xsl:text>&lt;/DebAccNo&gt;</xsl:text>
            <xsl:value-of select="substring-after(.,'&lt;/DebAccNo&gt;')" />
        </xsl:variable>
        <xsl:value-of select="$content" disable-output-escaping="yes"/> 
    </xsl:copy>
</xsl:template>

</xsl:stylesheet>