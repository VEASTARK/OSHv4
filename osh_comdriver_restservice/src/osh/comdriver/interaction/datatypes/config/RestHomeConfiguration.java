package osh.comdriver.interaction.datatypes.config;

import org.eclipse.persistence.annotations.PrivateOwned;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="shconfig")
@XmlRootElement(name="HomeConfiguration")
public class RestHomeConfiguration {
	@Id
	@GeneratedValue
	public Long id;
	
	@OneToMany(cascade={CascadeType.MERGE,CascadeType.PERSIST})
	@PrivateOwned  // deletes child elements, when parent element is deleted
	@XmlElement(name="element")
	public List<RestHomeConfigElement> elements;
	
	public List<RestHomeConfigElement> getElements() {
		if( elements == null ) {
			elements = new ArrayList<>();
		}
		return elements;
	}
	
	public void setElements(List<RestHomeConfigElement> elements) {
		this.elements = elements;
	}
}
