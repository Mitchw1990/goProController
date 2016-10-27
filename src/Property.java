import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Created by Mitch on 7/12/2016.
 */
public class Property{

    private StringProperty propertyId = new SimpleStringProperty(null);
    private StringProperty propertyAddress = new SimpleStringProperty(null);

    Property(String propertyId, String propertyAddress){
        this.propertyId.setValue(propertyId);
        this.propertyAddress.setValue(propertyAddress);
    }

    public String getPropertyId() {
        return propertyId.get();
    }

    public StringProperty propertyIdProperty() {
        return propertyId;
    }

    public void setPropertyId(String propertyId) {
        this.propertyId.set(propertyId);
    }

    public String getPropertyAddress() {
        return propertyAddress.get();
    }

    public StringProperty propertyAddressProperty() {
        return propertyAddress;
    }

    public void setPropertyAddress(String propertyAddress) {
        this.propertyAddress.set(propertyAddress);
    }

    @Override
    public String toString(){
        return "PID: " + propertyId + "\tAddress: " + propertyAddress;
    }
}
