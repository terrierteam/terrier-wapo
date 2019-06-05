package uk.ac.gla.terrier.indexing;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.terrier.indexing.TaggedDocument;
import org.terrier.indexing.TwitterJSONCollection;
import org.terrier.indexing.Document;
import org.terrier.indexing.tokenisation.Tokeniser;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/** A collection for the WAPO TREC test collection 
  * @author Craig Macdonald
  */
public class WAPOCollection extends TwitterJSONCollection {
	
	static final String[] METADATA_KEYS = new String[]{
		"id", "article_url", "published_date", "type", "source"
	};
	//static final Set<String> BLACKLIST_TYPE = Sets.immutableEnumSet(anElement, otherElements);
	
	static String parseWAPO(JsonObject doc, Map<String,String> docProps) {
		
		for(String s : METADATA_KEYS)
		{
			if (doc.has(s) && ! doc.get(s).isJsonNull())
				docProps.put(s, doc.get(s).getAsString());
		}
		docProps.put("docno", doc.get("id").getAsString());
		//System.err.println(doc.get("id").getAsString());
		
		StringBuilder rtr = new StringBuilder();
		if (doc.has("title") && ! doc.get("title").isJsonNull())
			rtr.append("<TITLE>" + doc.get("title").getAsString() + "</TITLE>\n");
		JsonArray ja = doc.getAsJsonArray("contents");
		for(JsonElement _contPiece : ja)
		{
			if (! _contPiece.isJsonObject())
				continue;
			JsonObject contPiece = _contPiece.getAsJsonObject();
			if ( (! contPiece.has("content")) || contPiece.get("content").isJsonNull())
				continue;
			String tagName = contPiece.get("type").getAsString().toUpperCase();
			rtr.append("<"+ tagName+ ">");
			if (contPiece.get("content").isJsonObject())
			{
				if (contPiece.has("blurb") && ! contPiece.get("blurb").isJsonNull())
					rtr.append(contPiece.get("blurb").getAsString());
				rtr.append(' ');
				if (contPiece.get("content").getAsJsonObject().has("title") && ! contPiece.get("content").getAsJsonObject().get("title").isJsonNull())
					rtr.append(contPiece.get("content").getAsJsonObject().get("title").getAsString());
			}
			else if (contPiece.get("content").isJsonArray())
			{
				for (JsonElement _contSubjpiece: contPiece.getAsJsonArray("content"))
				{
					rtr.append(_contSubjpiece.getAsString());
					rtr.append(' ');
				}
			}
			else
			{
				rtr.append(contPiece.get("content").getAsString());	
			}
			rtr.append("</"+ tagName+ ">\n");
		}
		
		return rtr.toString();
	}
	
	Tokeniser tokeniser = Tokeniser.getTokeniser();

	public WAPOCollection() {
		super();
	}

	public WAPOCollection(List<String> files, String TagSet, String BlacklistSpecFilename, String ignored) {
		super();
		FilesToProcess = new ArrayList<String>(files);
		FileNumber = -1;
		//open the first file
		try {
			openNextFile();
		} catch (IOException ioe) {
			logger.error("IOException opening first file of collection - is the collection.spec correct?", ioe);
		}
	}

	public WAPOCollection(String CollectionSpecFile) {
		super(CollectionSpecFile);
	}

	@Override
	public Document getDocument() {
		return currentDocument;
	}
	
	@Override
	public boolean nextDocument() {
		if (FilesToProcess==null) init();
		
		boolean nextOK = false;
		try {
			nextOK = JSONStream.hasNext();
		} catch (Exception e1) {
			logger.warn("Exception when checking if JSONStream has another document", e1);
		}
		
		if (nextOK) {
			
			final JsonObject jsonDoc = readTweet();
			Map<String,String> docProperties = new HashMap<String,String>();
			String doc = null;
			try{
				doc = parseWAPO(jsonDoc, docProperties);
			} catch (Exception e) {
				logger.error("problem parsing " + docProperties.get("docno"), e);
			}
			currentDocument = new TaggedDocument(
					new StringReader(doc),
					docProperties, 
					tokeniser);
			return true;
		} else {
			try {
				return openNextFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return false;
		
	}

}
