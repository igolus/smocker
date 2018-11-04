package com.jenetics.smocker.lucene;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.jboss.logging.Logger;

import com.jenetics.smocker.model.Communication;
import com.jenetics.smocker.model.EntityWithId;
import com.jenetics.smocker.util.NetworkReaderUtility;
import com.vaadin.ui.Notification;

public class LuceneIndexer {
	
	private static final String ID = "id";

	@Inject
	private static Logger logger;
	
	private StandardAnalyzer analyzer;
	private Directory index;
	private IndexWriterConfig config;
	private LuceneIndexer instance;
	private IndexWriter writer;
	private List<Communication> entities = new ArrayList<>(); 
	
	public LuceneIndexer() {
		
		super();
		analyzer = new StandardAnalyzer();
		index = new RAMDirectory();
        config = new IndexWriterConfig(analyzer);
        try {
			writer = new IndexWriter(index, config);
		} catch (IOException e) {
			logger.error("Unable to create writter ", e);
		}
	}
	
	public synchronized void addEntity(EntityWithId entity) {
		if (entity instanceof Communication) {
			Communication comm = (Communication) entity;
			entities.add(comm);
			Document doc = new Document();
	        doc.add(new TextField(ID, comm.getId().toString(), Field.Store.YES));
	        doc.add(new TextField("request", NetworkReaderUtility.decode(comm.getRequest()), Field.Store.YES));
	        doc.add(new TextField("response", NetworkReaderUtility.decode(comm.getResponse()), Field.Store.YES));
	        try {
				writer.addDocument(doc);
				writer.commit();
			} catch (IOException e) {
				logger.error("Unable to store document ", e);
			} 
		}
	}
	
	public synchronized List<Communication> search(String queryStr) {
		List<Communication> ret = new ArrayList();
		ret.addAll(search(queryStr, "request"));
		ret.addAll(search(queryStr, "response"));
		return ret;
	}
	
	public synchronized List<Communication> search(String queryStr, String field) {
		int hitsPerPage = 10;
        IndexReader reader;
		try {
			reader = DirectoryReader.open(index);
			IndexSearcher searcher = new IndexSearcher(reader);
	        Query query = new QueryParser(field, analyzer).parse(queryStr);
	        TopDocs docs = searcher.search(query, hitsPerPage);
	        ScoreDoc[] hits = docs.scoreDocs;
	        Notification.show("Found " + hits.length + " hits.");
	        List<Communication> foundComms = new ArrayList<>();
	        for(int i=0;i<hits.length;++i) {
	            int docId = hits[i].doc;
	            Document document = searcher.doc(docId);
	            String id = document.get(ID); 
	            Communication foundComm = entities.stream().filter( comm -> comm.getId() == Long.parseLong(id)).findFirst().orElse(null);
	            if (foundComm != null) {
	            	foundComms.add(foundComm);
	            }
	        }
	        return foundComms;
		} catch (IOException | ParseException e) {
			logger.error("Unable to search document ", e);
		}
        return null;
	}
}
