package modelset.search.lucene;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BooleanQuery.Builder;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import modelset.common.services.ISearchService;

public class LuceneSearchService implements ISearchService {

	private static final int MAX_CLAUSE = 2048;
	private String index_dir;
	
	public LuceneSearchService(String index_dir) {
		this.index_dir = index_dir;
	}
	
	@Override
	public @Nullable SearchResult search(@NonNull Resource r, int max) {
		try {
			BooleanQuery.setMaxClauseCount(MAX_CLAUSE);
			
			SearchResult result = new SearchResult();
			
			IndexSearcher searcher = createSearcher();
			//String txt = toText(r);
			
			Query q = toText(r);
			TopDocs foundDocs = searchContents(q, searcher);
			for (ScoreDoc sd : foundDocs.scoreDocs) {
				Document d = searcher.doc(sd.doc);
				String title = d.get("path");
				title = "data/" + new File(title).getName().replaceAll("txt$", "xmi");
				
				result.add(title, sd.score);
			}

			return result;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		
	}

	// https://howtodoinjava.com/lucene/lucene-index-search-examples/#lucene-search
	

	private IndexSearcher createSearcher() throws IOException 
	{
	    Directory dir = FSDirectory.open(Paths.get(index_dir));
	    IndexReader reader = DirectoryReader.open(dir);
	    IndexSearcher searcher = new IndexSearcher(reader);
	    return searcher;
	}
	
	 
	private static TopDocs searchContents(Query q, IndexSearcher searcher) throws Exception
	{
		//QueryParser qp = new QueryParser("contents", new StandardAnalyzer());
		// Query q = qp.parse(query);
		TopDocs hits = searcher.search(q, 300);
	    return hits;
	}
	
	/**
	 * Converts a Resource to a text just by extracting values from String attributes. 
	 */
	private BooleanQuery toText(@NonNull Resource r) {	
		List<BooleanClause> clauses = new ArrayList<BooleanClause>();
		Builder qbuilder = new BooleanQuery.Builder();
		
		
		StringBuilder builder = new StringBuilder();
		r.getAllContents().forEachRemaining(o -> {
			List<EAttribute> attrs = o.eClass().getEAllAttributes();
			for (EAttribute att : attrs) {
				if (att.isDerived())
					continue;
				
				Object value = o.eGet(att);
				if (value == null)
					continue;
				
				if (att.isMany()) {
					String text = ((List<Object>) value).stream().filter(v -> v instanceof String).map(v -> ((String) v).trim()).filter(v -> ! v.isEmpty()).collect(Collectors.joining(" "));
					if (! text.trim().isEmpty()) {
						builder.append(text);
						BooleanClause clause = new BooleanClause(new PhraseQuery("contents", text.split(" ")), Occur.SHOULD);
						clauses.add(clause);
						
						if (clauses.size() < MAX_CLAUSE) // To avoid max clause count exception
							qbuilder.add(clause);
					}
				} else {
					if (value instanceof String) {
						String str = (String) value;
						if (str.trim().isEmpty())
							continue;
						
						builder.append("\"" + value + "\"").append(" ");

						Query query;
						if (str.contains(" ")) 
							query = new PhraseQuery("contents", str.split(" "));
						else
							query = new TermQuery(new Term("contents", str));
									
						BooleanClause clause = new BooleanClause(query, Occur.SHOULD);
						clauses.add(clause);
						if (clauses.size() < MAX_CLAUSE) // To avoid max clause count exception
							qbuilder.add(clause);
					}
				}
			}
		});
		
		//BooleanClause[] array = clauses.toArray(new BooleanClause[clauses.size()]);
		return qbuilder.build();
		

		
		// return builder.toString();
	}

}
